#!/usr/bin/env bash

set -euo pipefail

INPUT_CSV="../src/main/resources/com/redpup/justsendit/model/shop/SkillCards.csv"

if [ ! -f "$INPUT_CSV" ]; then
    echo "Error: Input file '$INPUT_CSV' not found." >&2
    exit 1
fi

# Track created files so we can add headers to them exactly once
declare -A INITIALIZED_FILES

# Read CSV, handling headers and potential quotes/escaped commas smoothly via AWK
# We use AWK to parse the CSV correctly, then loop through the formatted lines in Bash
tail -n +2 "$INPUT_CSV" | awk -F',' '{
    # Clean carriage returns and extra quotes from fields
    for(i=1; i<=NF; i++) {
        gsub(/\r/, "", $i)
        # Strip leading/trailing quotes if they exist
        gsub(/^"/, "", $i)
        gsub(/"$/, "", $i)
    }

    # Map columns based on your sheet:
    # $1=Name, $2=Filename, $3=File, $4=Cost, $5=Green, $6=Blue, $7=Black, $8=Icons, $9=Category, $10=Text, $11=Flavor, $12=Copies
    print $1 "||" $2 "||" $3 "||" $4 "||" $5 "||" $6 "||" $7 "||" $8 "||" $9 "||" $10 "||" $11 "||" $12
}' | while IFS="||" read -r name filename file target_file cost green_dice blue_dice black_dice icons category text flavor_text copies || [ -n "$name" ]; do

    # Skip empty lines
    if [ -z "$name" ] && [ -z "$file_target" ]; then
        continue
    fi

    # Fallback to 'default' if File column is empty, otherwise sanitize the filename
    target_file="${file_target:-default}.textproto"

    # Initialize the file with proto headers if we haven't written to it yet in this run
    if [ -z "${INITIALIZED_FILES[$target_file]:-}" ]; then
        echo "# proto-file: path/to/your/schema.proto" > "$target_file"
        echo "# proto-message: SkillCardList" >> "$target_file"
        echo "" >> "$target_file"
        INITIALIZED_FILES[$target_file]=1
    fi

    # Determine number of copies (default to 1 if empty or not a number)
    num_copies="${copies:-1}"
    if ! [[ "$num_copies" =~ ^[0-9]+$ ]]; then
        num_copies=1
    fi

    # Generate the textproto card block string
    card_block=$(cat <<EOF
cards {
  filename: "$filename"
  name: "$name"
  cost: ${cost:-0}
  green_dice: ${green_dice:-0}
  blue_dice: ${blue_dice:-0}
  black_dice: ${black_dice:-0}
EOF
)

    # Append icons if present
    if [ -n "$icons" ]; then
        # Handle spaces or commas separating icons
        for icon in $(echo "$icons" | tr ',' ' '); do
            if [ -n "$icon" ]; then
                card_block+=$'\n'"  icons: $icon"
            fi
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
        echo "$card_block" >> "$target_file"
        echo "" >> "$target_file"
    done

done

echo "Conversion complete! Textproto files generated dynamically."