#!/usr/bin/env bash

set -euo pipefail

# --- CONFIGURATION CONSTANTS ---
INPUT_CSV="../resources/com/redpup/justsendit/model/shop/SkillCards.csv"
FILE_PREFIX="../resources/com/redpup/justsendit/model/shop/"
# -------------------------------

if [ ! -f "$INPUT_CSV" ]; then
    echo "Error: Input file '$INPUT_CSV' not found." >&2
    exit 1
fi

# Ensure the prefix directory exists if it's a path layout
if [ -n "$FILE_PREFIX" ] && [[ "$FILE_PREFIX" == */ ]]; then
    mkdir -p "$FILE_PREFIX"
fi

# Clean out old versions of the files we are about to generate in that directory
rm -f "${FILE_PREFIX}"*.textproto

# AWK dynamically maps headers to their index, processes dice/icons, and outputs structured lines
awk -F',' '
NR==1 {
    # Clean and map headers (forced to lowercase for bulletproof lookups)
    for(i=1; i<=NF; i++) {
        gsub(/\r/, "", $i)
        gsub(/^"/, "", $i)
        gsub(/"$/, "", $i)
        idx[tolower($i)] = i
    }
    next
}
{
    # Clean data fields
    for(i=1; i<=NF; i++) {
        gsub(/\r/, "", $i)
        gsub(/^"/, "", $i)
        gsub(/"$/, "", $i)
    }

    # 1. Process Dice counts from Die 1, Die 2, and Die 3
    green = 0; blue = 0; black = 0;
    for(i=1; i<=3; i++) {
        die_val = tolower($idx["die " i])
        if (die_val == "green") green++;
        else if (die_val == "blue") blue++;
        else if (die_val == "black") black++;
    }

    # 2. Combine Bonus Types into a space-separated string for icons
    # Filters out "none" or empty values automatically
    icons_str = ""
    for(i=1; i<=2; i++) {
        bonus_val = $idx["bonus type " i]
        if (bonus_val != "" && tolower(bonus_val) != "none") {
            if (icons_str == "") icons_str = bonus_val;
            else icons_str = icons_str " " bonus_val;
        }
    }

    # Print fields mapped dynamically by lowercase header name using our index array
    print $idx["title"] "||" \
          $idx["file"] "||" \
          $idx["cost"] "||" \
          green "||" \
          blue "||" \
          black "||" \
          icons_str "||" \
          $idx["effecttiming"] "||" \
          $idx["effecttext"] "||" \
          $idx["flavortext"] "||" \
          $idx["copies"]
}' "$INPUT_CSV" | while IFS="||" read -r name file_target cost green_dice blue_dice black_dice icons category text flavor_text copies || [ -n "$name" ]; do

    # Skip empty rows
    if [ -z "$name" ] && [ -z "$file_target" ]; then
        continue
    fi

    # Fallback to 'default' if File column is empty, sanitize, and prepend the prefix
    # NOTE: Filename field mapping in protobuf is populated using the name string converted to snake_case or whatever you prefer.
    # For now, it matches the lowercase file target base fallback pattern.
    base_filename="${file_target:-default}.textproto"
    target_filepath="${FILE_PREFIX}${base_filename}"
    filename_field=$(echo "$name" | tr '[:upper:]' '[:lower:]' | tr ' ' '_')

    # If the file doesn't exist yet, write the header
    if [ ! -f "$target_filepath" ]; then
        echo "# proto-file: path/to/your/schema.proto" > "$target_filepath"
        echo "# proto-message: SkillCardList" >> "$target_filepath"
        echo "" >> "$target_filepath"
    fi

    # Determine number of copies (default to 1 if empty or not a number)
    num_copies="${copies:-1}"
    if ! [[ "$num_copies" =~ ^[0-9]+$ ]]; then
        num_copies=1
    fi

    # Generate the textproto card block string
    card_block=$(cat <<EOF
cards {
  filename: "$filename_field"
  name: "$name"
  cost: ${cost:-0}
  green_dice: ${green_dice:-0}
  blue_dice: ${blue_dice:-0}
  black_dice: ${black_dice:-0}
EOF
)

    # Append icons if present
    if [ -n "$icons" ]; then
        for icon in $icons; do
            card_block+=$'\n'"  icons: $icon"
        done
    fi

    # Add category, text, and flavor text
    card_block+=$'\n'"  category: ${category:-EFFECT_CATEGORY_UNSET}"
    card_block+=$'\n'"  text: \"$text\""

    if [ -n "$flavor_text" ]; then
        card_block+=$'\n'"  flavor_text: \"$flavor_text\""
    fi

    card_block+=$'\n'"}"

    # Write the block to the specific target file multiple times based on the Copies count
    for ((i=0; i<num_copies; i++)); do
        echo "$card_block" >> "$target_filepath"
        echo "" >> "$target_filepath"
    done

done

echo "Conversion complete! Textproto files generated dynamically under '${FILE_PREFIX}'."
