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
SKILL_OUTPUT_FILENAME = "skill_cards.textproto"
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


# =====================================================================
# --- SKILL CARD TEXT -> STRUCTURED SkillCard TRANSLATION ---
# =====================================================================

DIE_ENUM_TYPE = "com.redpup.justsendit.model.Die"
DIE_ROLL_MESSAGE_TYPE = "com.redpup.justsendit.model.player.DieRoll"
DIE_ROLL_COLOR_FIELD = "die"
DIE_ROLL_VALUE_FIELD = "roll"

UNSUPPORTED_EFFECTS = []
UNSUPPORTED_CONDITIONS = []
UNSUPPORTED_REPEATS = []

_SIMPLE_DIE_EFFECT_RE = re.compile(r'^Reroll\s+(Green|Blue|Black|Wild)$')
_GAIN_STAT_RE = re.compile(r'^Gain \+(\d+) (Skill|Fun)$')
_GAIN_TAGS_RE = re.compile(r'^Gain tags x(\d+)$')
_GAIN_DICE_RE = re.compile(
  r'^Gain (Green|Blue|Black), Gain (Green|Blue|Black)$')
_GAIN_DIE_RE = re.compile(r'^Gain (Green|Blue|Black)$')
_GAIN_DIE_AND_FUN_RE = re.compile(r'^Gain (Green|Blue|Black) and \+(\d+) Fun$')
_REPLACE_DICE_RE = re.compile(
  r'^Replace all (Green|Blue|Black) with (Green|Blue|Black)$')
_REMOVE_DIE_RE = re.compile(r'^Remove (Green|Blue|Black|Wild)( X)?$')
_REPEAT_DIE_VALUE_RE = re.compile(r'^(Green|Blue|Black|Wild) (\d+)$')
_DIE_TYPE_RE = re.compile(r'^(Green|Blue|Black|Wild) die on other cards$')
_CARDS_IN_DECK_RE = re.compile(r'^(\d+) cards in your deck$')
_IGNORE_HAZARD_RE = re.compile(r'^Ignore (.*) on this tile$')


def _color_matcher_body(color: str, indent: str) -> str:
  if color == "WILD":
    return f"{indent}constant_matcher: true\n"
  return (
    f"{indent}enum_matcher {{\n"
    f"{indent}  enum_type_name: \"{DIE_ENUM_TYPE}\"\n"
    f"{indent}  name_matcher {{\n"
    f"{indent}    string_matcher {{ value: \"DIE_{color}\" }}\n"
    f"{indent}  }}\n"
    f"{indent}}}\n"
  )


def _alter_die_effect_block(color: str) -> str:
  return (
      "  effects {\n"
      "    alter_die {\n"
      "      die_matcher {\n"
      + _color_matcher_body(color, "        ")
      + "      }\n"
        "      reroll {}\n"
        "    }\n"
        "  }\n"
  )


def build_effect_action_blocks(effect_value: str, card_name: str) -> list:
  text = effect_value.strip()
  if not text:
    return []

  parts = [p.strip() for p in text.split(",")]
  if len(parts) > 1 and all(_SIMPLE_DIE_EFFECT_RE.match(p) for p in parts):
    return [
      _alter_die_effect_block(_SIMPLE_DIE_EFFECT_RE.match(p).group(1).upper())
      for p in parts]

  m = _SIMPLE_DIE_EFFECT_RE.match(text)
  if m:
    return [_alter_die_effect_block(m.group(1).upper())]

  m = _GAIN_DIE_AND_FUN_RE.match(text)
  if m:
    color, value = m.group(1).upper(), m.group(2)
    return [
      f"  effects {{\n    gain {{\n      die: DIE_{color}\n    }}\n  }}\n",
      f"  effects {{\n    gain {{\n      points: {value}\n    }}\n  }}\n",
    ]

  m = _GAIN_DICE_RE.match(text)
  if m:
    color1, color2 = m.group(1).upper(), m.group(2).upper()
    return [
      f"  effects {{\n    gain {{\n      die: DIE_{color1}\n    }}\n  }}\n",
      f"  effects {{\n    gain {{\n      die: DIE_{color2}\n    }}\n  }}\n",
    ]

  m = _GAIN_DIE_RE.match(text)
  if m:
    color = m.group(1).upper()
    return [
      f"  effects {{\n    gain {{\n      die: DIE_{color}\n    }}\n  }}\n"]

  if text == "Replace all Reroll with Gain":
    return [f"  effects {{\n    replace_rerolls_with_gains {{}}\n  }}\n"]

  m = _REPLACE_DICE_RE.match(text)
  if m:
    color1 = m.group(1).upper()
    color2 = m.group(2).upper()
    return [
      f"  effects {{\n    replace_dice {{\n      from: DIE_{color1}\n      to: DIE_{color2}\n    }}\n  }}\n"
    ]

  m = _GAIN_STAT_RE.match(text)
  if m:
    value, kind = m.groups()
    field = "skill" if kind == "Skill" else "points"
    return [
      f"  effects {{\n    gain {{\n      {field}: {value}\n    }}\n  }}\n"]

  m = _IGNORE_HAZARD_RE.match(text)
  if m:
    hazard = m.group(1).upper()
    return [f"  effects {{\n    ignore_hazard: HAZARD_{hazard}\n  }}\n"]

  if text == "Gain +X Fun":
    return [
      f"  effects {{\n    gain {{\n      points_linked: {{}}\n    }}\n  }}\n"]

  if text == "Gain Fun equal to value rolled":
    return ["  effects {\n    gain_fun_equal_to_value_rolled {}\n  }\n"]

  if text == "Gain tags of this card an additional time":
    return ["  effects {\n    gain_own_tags {}\n  }\n"]

  m = _GAIN_TAGS_RE.match(text)
  if m:
    value = m.group(1)
    return [f"  effects {{\n    gain_tags_below: {value} \n  }}\n"]

  if text == "Activate the effect an additional time":
    return ["  effects {\n    reactivate_following {}\n  }\n"]

  if text == "Discard any number of cards, then draw that many cards.":
    return ["  effects {\n    filter_hand {}\n  }\n"]

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

  if text == "Draw a card, then discard a card":
    return [
      "  effects {\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_TOPDECK\n"
      "      destination_zone: SKILL_CARD_ZONE_HAND\n"
      "      count: 1\n"
      "    }\n"
      "    card_effect {\n"
      "      source_zone: SKILL_CARD_ZONE_HAND\n"
      "      destination_zone: SKILL_CARD_ZONE_DISCARD\n"
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

  if text == "Look at the top 3 cards of your deck. Put 1 card on top and discard the others.":
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

  if text == "Trash a card":
    return ["  effects {\n    gain {\n      trashes: 1\n    }\n  }\n"]

  if text == "Return a card after this to your hand.":
    return ["  effects {\n    return_later_card {}\n  }\n"]

  if text == "Replenish the shop. Take another turn.":
    return [
      "  effects {\n    replenish_shop {}\n  }\n"
      "  effects {\n    extra_turn: EXTRA_TURN_REST \n  }\n"
    ]

  if text == "Put the card you buy on top of your deck instead of into your discard.":
    return ["  effects {\n    buy_to_topdeck {}\n  }\n"]

  if text == "Put the card you buy into your hand instead of into your discard.":
    return ["  effects {\n    buy_to_hand {}\n  }\n"]

  if text == "At the end of your turn, move one tile in any direction":
    return ["  effects {\n    move_tile {}\n  }\n"]

  UNSUPPORTED_EFFECTS.append((card_name, text))
  return [
    f'  # TODO(skill.proto): no field represents this effect yet: "{text}"\n']


def build_condition_and_cost_blocks(cond_text: str, own_cost: int,
    card_name: str) -> tuple:
  text = cond_text.strip()
  if not text:
    return None, None

  if text == "Success":
    return "  effect_condition {\n    success {}\n  }\n", None

  if text == "Failure":
    return "  effect_condition {\n    failure {}\n  }\n", None

  if text == "Next card costs X":
    return "  effect_condition {\n    next_card_cost_linked {}\n  }\n", None

  if text == "Next card costs greater":
    return (
      "  effect_condition {\n"
      "    next_card_cost {\n"
      "      comparison_matcher {\n"
      "        comparison: COMPARISON_GT\n"
      f"        int32_value: {own_cost}\n"
      "      }\n"
      "    }\n"
      "  }\n"
    ), None

  if text == "Discard a card":
    return None, "  effect_cost {\n    discard_card {}\n  }\n"

  m = _REMOVE_DIE_RE.match(text)
  if m:
    color = m.group(1).upper()
    return None, (
        "  effect_cost {\n"
        "    remove_die {\n"
        + _color_matcher_body(color, "      ")
        + "    }\n"
          "  }\n"
    )

  if text == "Next card":
    return None, ""

  UNSUPPORTED_CONDITIONS.append((card_name, text))
  return f'  # TODO(skill.proto): no condition/cost mapping for "{text}"\n', None


def _die_roll_value_field_block(value: str, indent: str) -> str:
  return (
    f"{indent}fields {{\n"
    f"{indent}  field_name: \"{DIE_ROLL_VALUE_FIELD}\"\n"
    f"{indent}  matcher {{\n"
    f"{indent}    collection_matcher {{\n"
    f"{indent}      last {{\n"
    f"{indent}        value_matcher {{ int32_value: {value} }}\n"
    f"{indent}      }}\n"
    f"{indent}    }}\n"
    f"{indent}  }}\n"
    f"{indent}}}\n"
  )


def build_repeat_block(repeat_text: str, card_name: str) -> str:
  text = repeat_text.strip()
  if not text:
    return None

  if text == "Matching tag on card above":
    return "  effect_repeat {\n    matching_tag_on_cards_above {}\n  }\n"

  if text == "Matching tag on other cards":
    return "  effect_repeat {\n    matching_tag_on_other_cards {}\n  }\n"

  if text == "Reroll on other cards":
    return "  effect_repeat {\n    reroll_on_other_cards {}\n  }\n"

  if text == "Set of Play / Ride / Finale / Rest":
    return ("  effect_repeat {\n    effect_category_set {\n"
            "      category: EFFECT_CATEGORY_PLAY\n"
            "      category: EFFECT_CATEGORY_RIDE\n"
            "      category: EFFECT_CATEGORY_FINALE\n"
            "      category: EFFECT_CATEGORY_REST\n"
            "    }\n  }\n")

  m = _REPEAT_DIE_VALUE_RE.match(text)
  if m:
    color, value = m.group(1).upper(), m.group(2)
    fields_block = _die_roll_value_field_block(value, "        ")
    if color != "WILD":
      fields_block = (
                         "        fields {\n"
                         f"          field_name: \"{DIE_ROLL_COLOR_FIELD}\"\n"
                         "          matcher {\n"
                         + _color_matcher_body(color, "            ")
                         + "          }\n"
                           "        }\n"
                     ) + fields_block

    return (
        "  effect_repeat {\n"
        "    matching_die {\n"
        "      message_matcher {\n"
        f"        message_type_name: \"{DIE_ROLL_MESSAGE_TYPE}\"\n"
        + fields_block +
        "      }\n"
        "    }\n"
        "  }\n"
    )

  m = _DIE_TYPE_RE.match(text)
  if m:
    color = m.group(1).upper()
    fields_block = ""
    if color != "WILD":
      fields_block = (
          "        fields {\n"
          f"          field_name: \"{DIE_ROLL_COLOR_FIELD}\"\n"
          "          matcher {\n"
          + _color_matcher_body(color, "            ")
          + "          }\n"
            "        }\n"
      )
    return (
        "  effect_repeat {\n"
        "    matching_die {\n"
        "      message_matcher {\n"
        f"        message_type_name: \"{DIE_ROLL_MESSAGE_TYPE}\"\n"
        + fields_block +
        "      }\n"
        "    }\n"
        "  }\n"
    )

  m = _CARDS_IN_DECK_RE.match(text)
  if m:
    count = m.group(1)
    return f"  effect_repeat {{\n    skill_cards_in_deck: {count}\n  }}\n"

  UNSUPPORTED_REPEATS.append((card_name, text))
  return f'  # TODO(skill.proto): no repeat mapping for "{text}"\n'


# =====================================================================
# --- PIPELINE IMPLEMENTATIONS ---
# =====================================================================

def process_skill_cards_pipeline():
  """Reads the skill cards CSV and outputs structured SkillCard textprotos."""
  if not os.path.isfile(SKILL_INPUT_CSV):
    print(f"Skipping Skill Cards: Source '{SKILL_INPUT_CSV}' not found.")
    return

  ensure_directory_exists(SKILL_FILE_PREFIX)
  target_filepath = os.path.join(SKILL_FILE_PREFIX, SKILL_OUTPUT_FILENAME)
  safe_delete_file(target_filepath)

  write_proto_file_header(
    target_filepath,
    "com/redpup/justsendit/model/supply/skill.proto",
    "SkillCardList"
  )

  with open(SKILL_INPUT_CSV, mode='r', newline='',
            encoding='utf-8') as csv_file:
    reader = csv.reader(csv_file)
    try:
      headers = next(reader)
    except StopIteration:
      return

    header_map = generate_header_lookup_map(headers)
    rows = list(reader)

    for row in rows:
      if not row or len(row) < len(header_map):
        continue

      name = get_safe_cell_value(row, header_map, "Title")
      file_target = get_safe_cell_value(row, header_map, "File")

      if not name and not file_target:
        continue

      img_filename = get_safe_cell_value(row, header_map, "Img Filename")
      filename_field = f"{SKILL_IMG_FILEPATH.rstrip('/')}/{img_filename.lstrip('/')}" if img_filename else ""

      file_lower = file_target.lower()
      if file_lower == "starter":
        skill_card_type = "SKILL_CARD_TYPE_STARTER"
      elif file_lower == "shop":
        skill_card_type = "SKILL_CARD_TYPE_UPGRADE"
      else:
        skill_card_type = "SKILL_CARD_TYPE_UNKNOWN"

      dice_colors = [
        get_safe_cell_value(row, header_map, f"Die {i}").lower()
        for i in range(1, 4)
      ]
      dice_enum_values = [
        f"DIE_{color.upper()}" for color in dice_colors
        if color in ("green", "blue", "black")
      ]

      icon_blocks = []
      for i in range(1, 3):
        bonus_val = get_safe_cell_value(row, header_map, f"Bonus Type {i}")
        if bonus_val and bonus_val.lower() != "none":
          msg_block = format_icon_message(bonus_val)
          if msg_block:
            icon_blocks.append(msg_block)

      cost_raw = get_safe_cell_value(row, header_map, "Cost")
      cost = int(cost_raw) if cost_raw.isdigit() else 0

      category_raw = get_safe_cell_value(row, header_map,
                                         "EffectTiming").upper()
      category_map = {
        "PLAY": "EFFECT_CATEGORY_PLAY",
        "REST": "EFFECT_CATEGORY_REST",
        "RIDE": "EFFECT_CATEGORY_RIDE",
        "LIFT": "EFFECT_CATEGORY_LIFT",
        "FINALE": "EFFECT_CATEGORY_FINALE",
        "NIGHT": "EFFECT_CATEGORY_NIGHT",
      }
      proto_category = category_map.get(category_raw, "EFFECT_CATEGORY_UNSET")

      cond_text = get_safe_cell_value(row, header_map, "EffectCost/Condition")
      condition_block, cost_block = build_condition_and_cost_blocks(cond_text,
                                                                    cost, name)

      effect_value_text = get_safe_cell_value(row, header_map, "EffectValue")
      effect_action_blocks = build_effect_action_blocks(effect_value_text,
                                                        name) if effect_value_text else []

      repeat_text = get_safe_cell_value(row, header_map, "EffectRepeat")
      repeat_block = build_repeat_block(repeat_text,
                                        name) if repeat_text else None

      flavor_text = get_safe_cell_value(row, header_map, "FlavorText")
      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

      # Computed fields from CSV
      dice_ev_str = get_safe_cell_value(row, header_map, "Dice EV")
      bonus_ev_str = get_safe_cell_value(row, header_map, "Bonus EV")
      effect_ev_str = get_safe_cell_value(row, header_map, "EffectEv")
      total_ev_str = get_safe_cell_value(row, header_map, "ExpectedValue")
      suggested_cost_str = get_safe_cell_value(row, header_map, "SuggestedCost")

      dice_ev = float(dice_ev_str) if dice_ev_str and dice_ev_str not in ("#REF!", "NaN") else 0.0
      bonus_ev = float(bonus_ev_str) if bonus_ev_str and bonus_ev_str not in ("#REF!", "NaN") else 0.0
      effect_ev = float(effect_ev_str) if effect_ev_str and effect_ev_str not in ("#REF!", "NaN") else 0.0
      total_ev = float(total_ev_str) if total_ev_str and total_ev_str not in ("#REF!", "NaN") else 0.0
      suggested_cost = int(float(suggested_cost_str)) if suggested_cost_str and suggested_cost_str not in ("#REF!", "NaN") else 0

      card_block = "cards {\n"
      if filename_field:
        card_block += f'  filename: "{escape_proto_string(filename_field)}"\n'
      card_block += f'  name: "{escape_proto_string(name)}"\n'
      if skill_card_type != "SKILL_CARD_TYPE_UNKNOWN":
        card_block += f'  type: {skill_card_type}\n'
      card_block += f'  cost: {cost}\n'
      for die_value in dice_enum_values:
        card_block += f'  dice: {die_value}\n'

      for icon in icon_blocks:
        card_block += icon + "\n"

      if proto_category != "EFFECT_CATEGORY_UNSET":
        card_block += f'  category: {proto_category}\n'

      if condition_block:
        card_block += condition_block
      if cost_block:
        card_block += cost_block

      for effect_block in effect_action_blocks:
        card_block += effect_block

      if repeat_block:
        card_block += repeat_block

      if flavor_text and flavor_text != "0":
        card_block += f'  flavor_text: "{escape_proto_string(flavor_text)}"\n'

      # Computed block
      card_block += "  computed {\n"
      if dice_ev != 0.0:
        card_block += f"    dice_expected_value: {dice_ev}\n"
      if bonus_ev != 0.0:
        card_block += f"    icon_expected_value: {bonus_ev}\n"
      if effect_ev != 0.0:
        card_block += f"    effect_expected_value: {effect_ev}\n"
      if total_ev != 0.0:
        card_block += f"    total_expected_value: {total_ev}\n"
      if suggested_cost > 0:
        card_block += f"    suggested_cost: {suggested_cost}\n"
      card_block += "  }\n"

      card_block += "}\n"

      append_blocks_to_proto(target_filepath, card_block, num_copies)

  print("Skill Cards processing finished successfully.")


def process_mountain_tiles_pipeline():
  """Reads the tiles CSV and outputs structured MountainTile textprotos supporting Slope and Lift variants."""
  if not os.path.isfile(TILE_INPUT_CSV):
    print(f"Skipping Mountain Tiles: Source '{TILE_INPUT_CSV}' not found.")
    return

  ensure_directory_exists(TILE_FILE_PREFIX)
  target_filepath = os.path.join(TILE_FILE_PREFIX, "mountain_tiles.textproto")
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

      tile_block = "tiles {\n"
      difficulty_raw = get_safe_cell_value(row, header_map, "Difficulty")
      success_raw = get_safe_cell_value(row, header_map, "Success")
      failure_raw = get_safe_cell_value(row, header_map, "Failure")
      grade_raw = get_safe_cell_value(row, header_map, "Grade")
      terrain_raw = get_safe_cell_value(row, header_map, "Terrain type")
      hazard_type = get_safe_cell_value(row, header_map, "Hazard Type")
      penalty_raw = get_safe_cell_value(row, header_map, "Penalty")

      lift_type = get_safe_cell_value(row, header_map, "LiftType")
      lift_end = get_safe_cell_value(row, header_map, "LiftEnd")
      lift_draw = get_safe_cell_value(row, header_map, "LiftDraw")
      lift_retire = get_safe_cell_value(row, header_map, "LiftRetire")

      if difficulty_raw.isnumeric():
        tile_block += f"  difficulty: {difficulty_raw}\n"
      if success_raw.isnumeric():
        tile_block += f"  success: {success_raw}\n"
      if failure_raw.isnumeric() or (failure_raw.startswith("-") and failure_raw[1:].isnumeric()):
        tile_block += f"  failure: {failure_raw}\n"

      proto_grade = clean_proto_enum_string("GRADE", grade_raw)
      if proto_grade != "GRADE_UNSET":
        tile_block += f"  grade: {proto_grade}\n"

      proto_condition = clean_proto_enum_string("CONDITION", terrain_raw)
      if proto_condition != "CONDITION_UNSET":
        tile_block += f"  condition: {proto_condition}\n"

      if hazard_type and hazard_type.lower() != "nan":
        hazard_penalty = int(penalty_raw) if penalty_raw and penalty_raw.lower() != "nan" else 1
        tile_block += "  hazard {\n"
        h_lower = hazard_type.lower()
        if "moguls blue to green" in h_lower:
          tile_block += "    moguls: MOGULS_BLUE_TO_GREEN\n"
        elif "moguls black to blue" in h_lower:
          tile_block += "    moguls: MOGULS_BLACK_TO_BLUE\n"
        elif "slow" in h_lower:
          tile_block += f"    slow: {hazard_penalty}\n"
        elif "trees" in h_lower:
          tile_block += f"    trees: {hazard_penalty}\n"
        elif "cliffs" in h_lower:
          tile_block += f"    cliffs: {hazard_penalty}\n"
        tile_block += "  }\n"

      if lift_type and lift_type.lower() != "nan":
        lift_lower = lift_type.lower()
        if "small" in lift_lower:
          tile_block += "  lift_size: LIFT_SIZE_SMALL\n"
        elif "medium" in lift_lower:
          tile_block += "  lift_size: LIFT_SIZE_MEDIUM\n"
        elif "large" in lift_lower:
          tile_block += "  lift_size: LIFT_SIZE_LARGE\n"

      if lift_end and lift_end.lower() != "nan":
        end_clean = lift_end.strip().upper()
        if end_clean in ["TOP", "BOTTOM"]:
          tile_block += f"  lift_direction: LIFT_DIRECTION_{end_clean}\n"

      if lift_draw.lower() == "true":
        tile_block += "  lift_draw: 1\n"
      if lift_retire.lower() == "true":
        tile_block += "  lift_retire: 1\n"

      tile_block += "}\n"
      append_blocks_to_proto(target_filepath, tile_block, 1)

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
