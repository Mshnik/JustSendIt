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


def escape_proto_string(value: str) -> str:
  """Escapes a raw string so it is safe to embed in a textproto string literal."""
  return value.replace("\\", "\\\\").replace('"', '\\"')


# =====================================================================
# --- DATA PARSING & SCHEMA CONVERSION HELPERS ---
# =====================================================================

def format_icon_message(bonus_val: str) -> str:
  """Translates spreadsheet strings to a deep nested protobuf message oneof structure."""
  val_lower = bonus_val.strip().lower()
  # Normalize whitespace to underscores so "Double Black" lines up with the
  # Grade enum's GRADE_DOUBLE_BLACK value.
  val_key = re.sub(r'\s+', '_', val_lower)

  if val_key in ["green", "blue", "black", "double_black"]:
    enum_val = f"GRADE_{val_key.upper()}"
    return f"  icons {{\n    grade: {enum_val}\n  }}"

  elif val_key in ["groomed", "powder", "ice"]:
    enum_val = f"CONDITION_{val_key.upper()}"
    return f"  icons {{\n    condition: {enum_val}\n  }}"

  elif val_key in ["moguls", "trees", "cliffs"]:
    enum_val = f"HAZARD_{val_key.upper()}"
    return f"  icons {{\n    hazard: {enum_val}\n  }}"

  elif val_key == "wild":
    return "  icons {\n    wild: true\n  }"

  return ""


def clean_proto_enum_string(prefix: str, enum_value: str) -> str:
  """Normalizes values to form fully qualified upper case enums."""
  if not enum_value or enum_value.lower() == "none" or enum_value == "0":
    return f"{prefix}_UNSET"

  sanitized = re.sub(r'\s+', '_', enum_value.strip().upper())
  return f"{prefix}_{sanitized}"


def parse_lift_card_bounds(text_val: str) -> tuple:
  """
  Scrapes the text column for lift card capacities.
  Returns a tuple containing (min_cards, max_cards).
  - If format is a range 'X-Y' -> returns (X, Y)
  - If format is a standalone number 'X' -> returns (X, X)
  - Otherwise returns (0, 0)
  """
  if not text_val:
    return 0, 0

  # Match 'X-Y' range format (allowing potential whitespace around the hyphen)
  range_match = re.match(r'^(\d+)\s*-\s*(\d+)$', text_val)
  if range_match:
    return int(range_match.group(1)), int(range_match.group(2))

  # Match single digit format 'X'
  if text_val.isdigit():
    val = int(text_val)
    return val, val

  return 0, 0


# =====================================================================
# --- SKILL EFFECT TEXT -> STRUCTURED SkillEffect TRANSLATION ---
# =====================================================================
#
# The SkillCard.proto message used to have a flat `text` field holding a
# human-readable effect description. It now has `repeated SkillEffect
# effects`, a fully structured representation. The spreadsheet still only
# gives us the human-readable "EffectText" column (plus a pile of derived
# boolean/EV helper columns used for spreadsheet math, which are NOT
# reliable indicators of effect *order* and are ignored here). Instead we
# parse the small, closed vocabulary of EffectText phrases directly into
# SkillEffect textproto blocks.
#
# Any effect phrase that doesn't match a known pattern falls through to a
# `# TODO(skill.proto)` comment (so the generated file still records author
# intent instead of silently dropping game logic) and is collected into
# UNSUPPORTED_EFFECTS so main() can print a summary at the end.
# =====================================================================

DIE_ENUM_TYPE = "com.redpup.justsendit.model.Die"

# Assumed nudge magnitude for "Slide <Color>" effects. The spreadsheet only
# tracks *that* a slide happens, not by how much, so this is a design
# placeholder -- adjust here if the game defines a different value.
DEFAULT_NUDGE_VALUE = 1

UNSUPPORTED_EFFECTS = []  # (card_name, effect_text) pairs collected for the final summary.

_SIMPLE_DIE_EFFECT_RE = re.compile(r'^(Reroll|Slide)\s+(Green|Blue|Black|Wild)$')
_WILD_VALUE_RE = re.compile(r'^Wild (\d+) -> \+2 Skill$')
_PER_CARD_RE = re.compile(r'^\+(\d+) (Skill|Fun) per card (above|below)$')
_PER_WOBBLE_RE = re.compile(r'^\+(\d+) Skill per Wobble$')
_FLAT_GAIN_RE = re.compile(r'^\+(\d+) (Skill|Fun)$')


def _die_matcher_block(color: str, indent: str) -> str:
  """Builds a `die_matcher { ... }` block matching a specific die color, or
  any die at all when color is WILD."""
  if color == "WILD":
    return f"{indent}die_matcher {{\n{indent}  constant_matcher: true\n{indent}}}\n"
  return (
    f"{indent}die_matcher {{\n"
    f"{indent}  enum_matcher {{\n"
    f"{indent}    enum_type_name: \"{DIE_ENUM_TYPE}\"\n"
    f"{indent}    name_matcher {{\n"
    f"{indent}      string_matcher {{ value: \"DIE_{color}\" }}\n"
    f"{indent}    }}\n"
    f"{indent}  }}\n"
    f"{indent}}}\n"
  )


def _alter_die_effect_block(color: str, op: str) -> str:
  """Builds one full `effects { alter_die { ... } }` block."""
  action = "reroll {}" if op == "reroll" else f"nudge: {DEFAULT_NUDGE_VALUE}"
  return (
      "  effects {\n"
      "    alter_die {\n"
      + _die_matcher_block(color, "      ")
      + f"      {action}\n"
        "    }\n"
        "  }\n"
  )


def build_effect_blocks(effect_text: str, card_name: str) -> list:
  """Converts one spreadsheet EffectText cell into a list of fully-formed
  `effects { ... }` textproto blocks (2-space indented, ready to be
  embedded directly inside a `cards { ... }` block)."""
  text = effect_text.strip()
  if not text:
    return []

  # Multiple simple die effects joined with a comma, e.g.
  # "Reroll Green, Slide Green" or "Reroll Wild, Reroll Wild" each become
  # their own SkillEffect entry, in the order they're listed.
  parts = [p.strip() for p in text.split(",")]
  if len(parts) > 1 and all(_SIMPLE_DIE_EFFECT_RE.match(p) for p in parts):
    blocks = []
    for part in parts:
      m = _SIMPLE_DIE_EFFECT_RE.match(part)
      op = "reroll" if m.group(1) == "Reroll" else "nudge"
      blocks.append(_alter_die_effect_block(m.group(2).upper(), op))
    return blocks

  m = _SIMPLE_DIE_EFFECT_RE.match(text)
  if m:
    op = "reroll" if m.group(1) == "Reroll" else "nudge"
    return [_alter_die_effect_block(m.group(2).upper(), op)]

  # "Wild N -> +2 Skill": gain 2 skill once per die (any color) currently
  # showing the face value N.
  m = _WILD_VALUE_RE.match(text)
  if m:
    n = m.group(1)
    return [
      "  effects {\n"
      "    gain {\n"
      "      skill: 2\n"
      "      matching_die {\n"
      f"        value_matcher {{ int32_value: {n} }}\n"
      "      }\n"
      "    }\n"
      "  }\n"
    ]

  # "+N Skill/Fun per card above/below"
  m = _PER_CARD_RE.match(text)
  if m:
    value, kind, position = m.groups()
    field = "skill" if kind == "Skill" else "points"
    repeat = "skill_card_above" if position == "above" else "skill_card_below"
    return [
      "  effects {\n"
      "    gain {\n"
      f"      {field}: {value}\n"
      f"      {repeat} {{}}\n"
      "    }\n"
      "  }\n"
    ]

  # "+N Skill per Wobble"
  m = _PER_WOBBLE_RE.match(text)
  if m:
    value = m.group(1)
    return [
      "  effects {\n"
      "    gain {\n"
      f"      skill: {value}\n"
      "      wobble {}\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Success -> +9 Fun":
    return [
      "  effects {\n"
      "    success {}\n"
      "    gain {\n"
      "      points: 9\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Success -> Draw a card":
    return [
      "  effects {\n"
      "    success {}\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_HAND\n"
      "      count: 1\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Discard a card -> +5 Skill":
    return [
      "  effects {\n"
      "    discard_card {}\n"
      "    gain {\n"
      "      skill: 5\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Discard a card -> Draw a card":
    return [
      "  effects {\n"
      "    discard_card {}\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_HAND\n"
      "      count: 1\n"
      "    }\n"
      "  }\n"
    ]

  # "+N Skill/Fun" flat, unconditional gain (must be checked after the
  # "per card"/"per Wobble" variants above, since those also start with '+').
  m = _FLAT_GAIN_RE.match(text)
  if m:
    value, kind = m.groups()
    field = "skill" if kind == "Skill" else "points"
    return [
      "  effects {\n"
      "    gain {\n"
      f"      {field}: {value}\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Draw a card":
    return [
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_HAND\n"
      "      count: 1\n"
      "    }\n"
      "  }\n"
    ]

  if text == "Draw 2 cards, then put 2 cards on top of your deck.":
    return [
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_HAND\n"
      "      count: 2\n"
      "    }\n"
      "  }\n",
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_HAND\n"
      "      destination_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      count: 2\n"
      "    }\n"
      "  }\n",
    ]

  if text == "Ignore 1 Wobble":
    return ["  effects {\n    ignore_wobble {}\n  }\n"]

  if text == "Activate the effect of the card below an additional time.":
    return ["  effects {\n    reactivate_following {}\n  }\n"]

  if text == "Discard any number of cards, then draw that many cards.":
    return ["  effects {\n    filter_hand {}\n  }\n"]

  if text == "Trash an additional card.":
    return ["  effects {\n    gain {\n      trashes: 1\n    }\n  }\n"]

  if text == "You may buy an additional card this turn (you must pay both costs).":
    return ["  effects {\n    gain {\n      buys: 1\n    }\n  }\n"]

  if text == "Replenish the shop.":
    return ["  effects {\n    replenish_shop {}\n  }\n"]

  if text == "Take another turn after this one.":
    return ["  effects {\n    extra_turn {}\n  }\n"]

  if text == "Look at the top 3 cards of your deck. Put 1 card on top and discard the others.":
    # Reveal the top 3, discard 2 of them, and return the 1 that's kept to
    # the top of the deck.
    return [
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_REVEALED_TOPDECK\n"
      "      count: 3\n"
      "    }\n"
      "  }\n",
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_REVEALED_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_DISCARD\n"
      "      count: 2\n"
      "    }\n"
      "  }\n",
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_REVEALED_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      count: 1\n"
      "    }\n"
      "  }\n",
    ]

  # Nothing matched: this EffectText has no representation in skill.proto
  # yet. Emit a comment so the generated file still records the intended
  # design, and surface it in the end-of-run summary instead of failing
  # silently.
  UNSUPPORTED_EFFECTS.append((card_name, text))
  return [f'  # TODO(skill.proto): no field represents this effect yet: "{text}"\n']


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
      category_map = {
        "PLAY": "EFFECT_CATEGORY_PLAY",
        "FIRST": "EFFECT_CATEGORY_FIRST",
        "LAST": "EFFECT_CATEGORY_LAST",
        "PASS": "EFFECT_CATEGORY_PASS",
        "LIFT": "EFFECT_CATEGORY_LIFT",
      }
      proto_category = category_map.get(category_raw, "EFFECT_CATEGORY_UNSET")

      effect_text = get_safe_cell_value(row, header_map, "EffectText")
      if effect_text == "0":
        effect_text = ""
      effect_blocks = build_effect_blocks(effect_text, name) if effect_text else []

      flavor_text = get_safe_cell_value(row, header_map, "FlavorText")

      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      card_block = "cards {\n"
      if filename_field:
        card_block += f'  filename: "{escape_proto_string(filename_field)}"\n'
      card_block += f'  name: "{escape_proto_string(name)}"\n'
      card_block += f'  cost: {cost}\n'
      card_block += f'  green_dice: {green_dice}\n'
      card_block += f'  blue_dice: {blue_dice}\n'
      card_block += f'  black_dice: {black_dice}\n'

      for icon in icon_blocks:
        card_block += icon + "\n"

      if proto_category != "EFFECT_CATEGORY_UNSET":
        card_block += f'  category: {proto_category}\n'

      for effect_block in effect_blocks:
        card_block += effect_block

      if flavor_text and flavor_text != "0":
        card_block += f'  flavor_text: "{escape_proto_string(flavor_text)}"\n'
      card_block += "}\n"

      append_blocks_to_proto(target_filepath, card_block, num_copies)

  if UNSUPPORTED_EFFECTS:
    print("Warning: the following effects have no corresponding field in "
          "skill.proto yet and were emitted as comments only:")
    for card_name, text in UNSUPPORTED_EFFECTS:
      print(f'  - {card_name}: "{text}"')

  print("Skill Cards processing finished successfully.")


def process_mountain_tiles_pipeline():
  """Reads the tiles CSV and outputs structured MountainTile textprotos supporting Slope and Lift variants."""
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

      tile_type = get_safe_cell_value(row, header_map, "Type").lower()
      if tile_type not in ["slope", "lift"]:
        continue

      # Common MountainTile Root Properties
      tile_filename = get_safe_cell_value(row, header_map, "Img Filename")
      apres_raw = get_safe_cell_value(row, header_map, "Apres Link")
      apres_link = int(apres_raw) if apres_raw.isdigit() else 0

      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      tile_block = "tiles {\n"

      # --- BRANCH 1: SLOPE TILE HANDLING ---
      if tile_type == "slope":
        grade_raw = get_safe_cell_value(row, header_map, "Grade")
        slow_raw = get_safe_cell_value(row, header_map, "Slow").upper()
        is_slow = "true" if slow_raw == "TRUE" else "false"

        difficulty_raw = get_safe_cell_value(row, header_map, "Text")
        difficulty = int(difficulty_raw) if difficulty_raw.isdigit() else 1

        proto_grade = clean_proto_enum_string("GRADE", grade_raw)
        proto_condition = clean_proto_enum_string("CONDITION", get_safe_cell_value(row, header_map, "Terrain type"))

        hazards = []
        for i in range(1, 3):
          hazard_val = get_safe_cell_value(row, header_map, f"Hazard Type {i}")
          if hazard_val and hazard_val.lower() != "none" and hazard_val != "0":
            hazards.append(clean_proto_enum_string("HAZARD", hazard_val))

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

      # --- BRANCH 2: LIFT TILE HANDLING ---
      elif tile_type == "lift":
        lift_color_raw = get_safe_cell_value(row, header_map, "LiftColor")
        lift_dir_raw = get_safe_cell_value(row, header_map, "LiftEnd")

        proto_lift_color = clean_proto_enum_string("LIFT_COLOR", lift_color_raw)
        proto_lift_dir = clean_proto_enum_string("LIFT_DIRECTION", lift_dir_raw)

        # Scrape and parse the 'Text' column dynamically for capacity ranges
        text_column_val = get_safe_cell_value(row, header_map, "Text")
        min_cards, max_cards = parse_lift_card_bounds(text_column_val)

        tile_block += "  lift {\n"
        if proto_lift_color != "LIFT_COLOR_UNSET":
          tile_block += f"    color: {proto_lift_color}\n"
        if proto_lift_dir != "LIFT_DIRECTION_UNSET":
          tile_block += f"    direction: {proto_lift_dir}\n"
        if min_cards > 0:
          tile_block += f"    min_cards: {min_cards}\n"
        if max_cards > 0:
          tile_block += f"    max_cards: {max_cards}\n"
        tile_block += "  }\n"

      # --- ROOT assignment blocks ---
      if apres_link > 0:
        tile_block += f"  apres_link: {apres_link}\n"
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
