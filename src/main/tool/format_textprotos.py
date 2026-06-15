#!/usr/bin/env python3
import csv
import os
import re

# =====================================================================
# --- CONFIGURATION CONSTANTS ---
# =====================================================================
SKILL_INPUT_CSV = \
  "../resources/com/redpup/justsendit/model/shop/skill/SkillCards.csv"
SKILL_FILE_PREFIX = "../resources/com/redpup/justsendit/model/shop/skill/"
SKILL_IMG_FILEPATH = "src/main/resources/com/redpup/justsendit/img/skill_cards/"

TILE_INPUT_CSV = "../resources/com/redpup/justsendit/model/board/tile/Tiles.csv"
TILE_FILE_PREFIX = "../resources/com/redpup/justsendit/model/board/tile/"
# =====================================================================


# =====================================================================
# --- CORE INFRASTRUCTURE UTILITIES ---
# =====================================================================

def ensure_directory_exists(prefix_dir: str):
  """Ensures the target directory structure exists on disk."""
  if prefix_dir and prefix_dir.endswith('/'):
    os.makedirs(prefix_dir, exist_ok=True)


def safe_delete_file(filepath: str):
  """Removes a file safely if it exists without throwing errors."""
  if os.path.isfile(filepath):
    os.remove(filepath)


def write_proto_file_header(target_filepath: str, proto_import_path: str,
    message_type: str):
  """Writes the top-level diagnostic metadata comments to an empty textproto."""
  with open(target_filepath, mode='w', encoding='utf-8') as f:
    f.write(f"# proto-file: {proto_import_path}\n")
    f.write(f"# proto-message: {message_type}\n\n")


def generate_header_lookup_map(headers_list: list) -> dict:
  """Normalizes string column arrays into lowercase index keys."""
  return {header.strip().lower(): idx for idx, header in
          enumerate(headers_list)}


def get_safe_cell_value(row: list, header_map: dict, target_header: str,
    default: str = "") -> str:
  """Looks up cell data out of a data array by column name."""
  idx = header_map.get(target_header.lower())
  return row[idx].strip() if idx is not None and idx < len(row) else default


def append_blocks_to_proto(target_filepath: str, block_text: str,
    copies_count: int):
  """Appends generated text representations to a given target file."""
  with open(target_filepath, mode='a', encoding='utf-8') as f:
    for _ in range(copies_count):
      f.write(block_text + "\n")


# =====================================================================
# --- DATA PARSING & SCHEMA CONVERSION HELPERS ---
# =====================================================================

def format_icon_message(bonus_val: str) -> str:
  """Translates spreadsheet strings to a deep nested protobuf message oneof structure."""
  val_lower = bonus_val.strip().lower()

  if val_lower in ["green", "blue", "black", "doubleblack"]:
    enum_val = f"GRADE_{bonus_val.strip().upper()}"
    return f"  icons {{\n    grade: {enum_val}\n  }}"

  elif val_lower in ["groomed", "powder", "ice"]:
    enum_val = f"CONDITION_{bonus_val.strip().upper()}"
    return f"  icons {{\n    condition: {enum_val}\n  }}"

  elif val_lower in ["moguls", "trees", "cliffs"]:
    enum_val = f"HAZARD_{bonus_val.strip().upper()}"
    return f"  icons {{\n    hazard: {enum_val}\n  }}"

  elif val_lower == "wild":
    return "  icons {\n    wild: true\n  }"

  return ""


def clean_proto_enum_string(prefix: str, enum_value: str) -> str:
  """Normalizes values to form fully qualified upper case enums."""
  if not enum_value or enum_value.lower() == "none" or enum_value == "0":
    return f"{prefix}_UNSET"

  sanitized = re.sub(r'\s+', '_', enum_value.strip().upper())
  return f"{prefix}_{sanitized}"


# =====================================================================
# --- PIPELINE IMPLEMENTATIONS ---
# =====================================================================

def process_skill_cards_pipeline():
  """Reads the skill cards CSV and outputs structured SkillCard textprotos."""
  if not os.path.isfile(SKILL_INPUT_CSV):
    print(f"Skipping Skill Cards: Source '{SKILL_INPUT_CSV}' not found.")
    return

  ensure_directory_exists(SKILL_FILE_PREFIX)
  initialized_files = set()

  with open(SKILL_INPUT_CSV, mode='r', newline='', encoding='utf-8') as csv_file:
    reader = csv.reader(csv_file)
    try:
      headers = next(reader)
    except StopIteration:
      return

    header_map = generate_header_lookup_map(headers)
    rows = list(reader)

    # 1. First Pass: Safely identify and delete only the specific files we will modify
    for row in rows:
      if not row or len(row) < len(header_map):
        continue
      file_target = get_safe_cell_value(row, header_map, "File")
      if file_target or get_safe_cell_value(row, header_map, "Title"):
        base_filename = f"{file_target if file_target else 'default'}.textproto"
        safe_delete_file(os.path.join(SKILL_FILE_PREFIX, base_filename))

    # 2. Second Pass: Process rows and write data
    for row in rows:
      if not row or len(row) < len(header_map):
        continue

      name = get_safe_cell_value(row, header_map, "Title")
      file_target = get_safe_cell_value(row, header_map, "File")

      if not name and not file_target:
        continue

      base_filename = f"{file_target if file_target else 'default'}.textproto"
      target_filepath = os.path.join(SKILL_FILE_PREFIX, base_filename)

      img_filename = get_safe_cell_value(row, header_map, "Img Filename")
      filename_field = f"{SKILL_IMG_FILEPATH.rstrip('/')}/{img_filename.lstrip('/')}" if img_filename else ""

      if target_filepath not in initialized_files:
        write_proto_file_header(
          target_filepath,
          "com/redpup/justsendit/model/supply/skill.proto",
          "SkillCardList"
        )
        initialized_files.add(target_filepath)

      # Process dice
      green_dice = sum(1 for i in range(1, 4) if get_safe_cell_value(row, header_map, f"Die {i}").lower() == "green")
      blue_dice = sum(1 for i in range(1, 4) if get_safe_cell_value(row, header_map, f"Die {i}").lower() == "blue")
      black_dice = sum(1 for i in range(1, 4) if get_safe_cell_value(row, header_map, f"Die {i}").lower() == "black")

      # Process icons
      icon_blocks = []
      for i in range(1, 3):
        bonus_val = get_safe_cell_value(row, header_map, f"Bonus Type {i}")
        if bonus_val and bonus_val.lower() != "none":
          msg_block = format_icon_message(bonus_val)
          if msg_block:
            icon_blocks.append(msg_block)

      cost_raw = get_safe_cell_value(row, header_map, "Cost")
      cost = int(cost_raw) if cost_raw.isdigit() else 0

      category_raw = get_safe_cell_value(row, header_map, "EffectTiming").upper()
      category_map = {"PLAY": "EFFECT_CATEGORY_PLAY", "NEXT": "EFFECT_CATEGORY_NEXT", "PASS": "EFFECT_CATEGORY_PASS", "LIFT": "EFFECT_CATEGORY_LIFT"}
      proto_category = category_map.get(category_raw, "EFFECT_CATEGORY_UNSET")

      text = get_safe_cell_value(row, header_map, "EffectText")
      if text == "0":
        text = ""
      flavor_text = get_safe_cell_value(row, header_map, "FlavorText")

      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      card_block = "cards {\n"
      if filename_field:
        card_block += f'  filename: "{filename_field}"\n'
      card_block += f'  name: "{name}"\n'
      card_block += f'  cost: {cost}\n'
      card_block += f'  green_dice: {green_dice}\n'
      card_block += f'  blue_dice: {blue_dice}\n'
      card_block += f'  black_dice: {black_dice}\n'

      for icon in icon_blocks:
        card_block += icon + "\n"

      if proto_category != "EFFECT_CATEGORY_UNSET":
        card_block += f'  category: {proto_category}\n'
      if text:
        card_block += f'  text: "{text}"\n'
      if flavor_text and flavor_text != "0":
        card_block += f'  flavor_text: "{flavor_text}"\n'
      card_block += "}\n"

      append_blocks_to_proto(target_filepath, card_block, num_copies)

  print("Skill Cards processing finished successfully.")


def process_mountain_tiles_pipeline():
  """Reads the tiles CSV and outputs structured MountainTile textprotos."""
  if not os.path.isfile(TILE_INPUT_CSV):
    print(f"Skipping Mountain Tiles: Source '{TILE_INPUT_CSV}' not found.")
    return

  ensure_directory_exists(TILE_FILE_PREFIX)
  target_filepath = os.path.join(TILE_FILE_PREFIX, "mountain_tiles.textproto")

  # Clean out ONLY this specific generated textproto file
  safe_delete_file(target_filepath)

  write_proto_file_header(target_filepath,
                          "com/redpup/justsendit/model/board/tile/tile.proto",
                          "MountainTileList")

  with open(TILE_INPUT_CSV, mode='r', newline='', encoding='utf-8') as csv_file:
    reader = csv.reader(csv_file)
    try:
      headers = next(reader)
    except StopIteration:
      return

    header_map = generate_header_lookup_map(headers)

    for row in reader:
      if not row or len(row) < len(header_map):
        continue

      grade_raw = get_safe_cell_value(row, header_map, "Grade")
      if not grade_raw:
        continue

      slow_raw = get_safe_cell_value(row, header_map, "Slow").upper()
      is_slow = "true" if slow_raw == "TRUE" else "false"

      difficulty_raw = get_safe_cell_value(row, header_map, "Text")
      difficulty = int(difficulty_raw) if difficulty_raw.isdigit() else 1

      proto_grade = clean_proto_enum_string("GRADE", grade_raw)
      proto_condition = clean_proto_enum_string("CONDITION", get_safe_cell_value(row, header_map, "Terrain type"))

      # Parse visual image layout string column
      tile_filename = get_safe_cell_value(row, header_map, "Img Filename")

      hazards = []
      for i in range(1, 3):
        hazard_val = get_safe_cell_value(row, header_map, f"Hazard Type {i}")
        if hazard_val and hazard_val.lower() != "none" and hazard_val != "0":
          hazards.append(clean_proto_enum_string("HAZARD", hazard_val))

      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      # Build nested structures mapping 'filename' dynamically to the MountainTile root
      tile_block = "tiles {\n"
      tile_block += "  slope {\n"
      tile_block += f"    difficulty: {difficulty}\n"
      if proto_grade != "GRADE_UNSET":
        tile_block += f"    grade: {proto_grade}\n"
      if proto_condition != "CONDITION_UNSET":
        tile_block += f"    condition: {proto_condition}\n"

      for haz in hazards:
        tile_block += f"    hazards: {haz}\n"

      tile_block += f"    slow: {is_slow}\n"
      tile_block += "  }\n"
      if tile_filename:
        tile_block += f'  filename: "{tile_filename}"\n'
      tile_block += "}\n"

      append_blocks_to_proto(target_filepath, tile_block, num_copies)

  print("Mountain Tiles processing finished successfully.")


# =====================================================================
# --- SYSTEM MAIN ENTRY POINT ---
# =====================================================================

def main():
  print("Initiating proto data generation engine...")
  process_skill_cards_pipeline()
  process_mountain_tiles_pipeline()
  print("All conversions complete!")


if __name__ == "__main__":
  main()
