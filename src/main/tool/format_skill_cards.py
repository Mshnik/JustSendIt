#!/usr/bin/env python3
import csv
import os

# --- CONFIGURATION CONSTANTS ---
INPUT_CSV = "../resources/com/redpup/justsendit/model/shop/SkillCards.csv"
FILE_PREFIX = "../resources/com/redpup/justsendit/model/shop/"
IMG_FILEPATH = "src/main/resources/com/redpup/justsendit/img/skill_cards/"
# -------------------------------

def format_icon_message(bonus_val):
  """
  Translates a spreadsheet Bonus Type into a nested protobuf message layout.
  Example: 'Green' ->
  icons {
    grade: GRADE_GREEN
  }
  """
  val_lower = bonus_val.strip().lower()

  # 1. Check if it's a Grade
  if val_lower in ["green", "blue", "black", "doubleblack"]:
    enum_val = f"GRADE_{bonus_val.strip().upper()}"
    return f"  icons {{\n    grade: {enum_val}\n  }}"

  # 2. Check if it's a Condition
  elif val_lower in ["groomed", "powder", "ice"]:
    enum_val = f"CONDITION_{bonus_val.strip().upper()}"
    return f"  icons {{\n    condition: {enum_val}\n  }}"

  # 3. Check if it's a Hazard
  elif val_lower in ["moguls", "trees", "cliffs"]:
    enum_val = f"HAZARD_{bonus_val.strip().upper()}"
    return f"  icons {{\n    hazard: {enum_val}\n  }}"

  # 4. Check if it's Wild
  elif val_lower == "wild":
    return "  icons {\n    wild: true\n  }"

  return None


def main():
  if not os.path.isfile(INPUT_CSV):
    print(f"Error: Input file '{INPUT_CSV}' not found.")
    return

  # Ensure prefix directory exists
  if FILE_PREFIX and FILE_PREFIX.endswith('/'):
    os.makedirs(FILE_PREFIX, exist_ok=True)

  # Clean out old textproto files in the destination directory
  for filename in os.listdir(FILE_PREFIX):
    if filename.endswith(".textproto"):
      os.remove(os.path.join(FILE_PREFIX, filename))

  # Keep track of which files have received their textproto headers
  initialized_files = set()

  with open(INPUT_CSV, mode='r', newline='', encoding='utf-8') as csv_file:
    reader = csv.reader(csv_file)
    try:
      headers = next(reader)
    except StopIteration:
      print("Error: CSV file is empty.")
      return

    # Normalize headers to lowercase to ensure robust lookup mapping
    header_map = {header.strip().lower(): idx for idx, header in
                  enumerate(headers)}

    for row in reader:
      if not row or len(row) < len(header_map):
        continue

      def get_val(header_name, default=""):
        idx = header_map.get(header_name.lower())
        return row[idx].strip() if idx is not None and idx < len(
          row) else default

      name = get_val("Title")
      file_target = get_val("File")

      if not name and not file_target:
        continue

      base_filename = f"{file_target if file_target else 'default'}.textproto"
      target_filepath = os.path.join(FILE_PREFIX, base_filename)

      img_filename = get_val("Img Filename")
      if img_filename:
        # Safely construct the path using a forward slash for cross-platform proto string compatibility
        clean_prefix = IMG_FILEPATH.rstrip('/')
        clean_img = img_filename.lstrip('/')
        filename_field = f"{clean_prefix}/{clean_img}"
      else:
        filename_field = ""

      # Write header to target file if it's the first time accessing it this run
      if target_filepath not in initialized_files:
        with open(target_filepath, mode='w', encoding='utf-8') as f:
          f.write(
            "# proto-file: com/redpup/justsendit/model/supply/skill.proto\n")
          f.write("# proto-message: SkillCardList\n\n")
        initialized_files.add(target_filepath)

      # --- 1. PROCESS DICE ---
      green_dice = 0
      blue_dice = 0
      black_dice = 0
      for i in range(1, 4):
        die_val = get_val(f"Die {i}").lower()
        if die_val == "green":
          green_dice += 1
        elif die_val == "blue":
          blue_dice += 1
        elif die_val == "black":
          black_dice += 1

      # --- 2. PROCESS ICONS (NESTED MESSAGES) ---
      icon_blocks = []
      for i in range(1, 3):
        bonus_val = get_val(f"Bonus Type {i}")
        if bonus_val and bonus_val.lower() != "none":
          formatted_msg = format_icon_message(bonus_val)
          if formatted_msg:
            icon_blocks.append(formatted_msg)

      # --- 3. PROCESS COST ---
      cost_raw = get_val("Cost")
      cost = int(cost_raw) if cost_raw.isdigit() else 0

      # --- 4. PROCESS EFFECT TIMING ENUM ---
      category_raw = get_val("EffectTiming").upper()
      category_map = {
        "PLAY": "EFFECT_CATEGORY_PLAY",
        "NEXT": "EFFECT_CATEGORY_NEXT",
        "PASS": "EFFECT_CATEGORY_PASS",
        "LIFT": "EFFECT_CATEGORY_LIFT"
      }
      proto_category = category_map.get(category_raw, "EFFECT_CATEGORY_UNSET")

      # --- 5. CLEAN TEXT STRINGS ---
      text = get_val("EffectText")
      if text == "0":
        text = ""

      flavor_text = get_val("FlavorText")

      # --- 6. PROCESS COPIES COUNT ---
      copies_raw = get_val("Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      # --- BUILD TEXTPROTO CARD BLOCK ---
      card_block = "cards {\n"
      card_block += f'  filename: "{filename_field}"\n'
      card_block += f'  name: "{name}"\n'
      card_block += f'  cost: {cost}\n'
      card_block += f'  green_dice: {green_dice}\n'
      card_block += f'  blue_dice: {blue_dice}\n'
      card_block += f'  black_dice: {black_dice}\n'

      # Append the structured message representations for icons
      for block in icon_blocks:
        card_block += block + "\n"

      # If category is UNSET, omit it entirely from the proto text representation
      if proto_category != "EFFECT_CATEGORY_UNSET":
        card_block += f'  category: {proto_category}\n'

      # If text is empty, omit it entirely from the proto text representation
      if text:
        card_block += f'  text: "{text}"\n'

      if flavor_text and flavor_text != "0":
        card_block += f'  flavor_text: "{flavor_text}"\n'

      card_block += "}\n"

      # Append the block to the specific target file for each required copy
      with open(target_filepath, mode='a', encoding='utf-8') as f:
        for _ in range(num_copies):
          f.write(card_block + "\n")

  print(
    f"Conversion complete! Textproto files generated dynamically under '{FILE_PREFIX}'.")


if __name__ == "__main__":
  main()
