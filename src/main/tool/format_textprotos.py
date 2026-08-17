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
# --- SKILL CARD TEXT -> STRUCTURED SkillCard TRANSLATION ---
# =====================================================================
#
# The spreadsheet now splits an effect's timing (EffectTiming), its
# cost/condition (EffectCost/Condition), its action(s) (EffectValue), and
# its repeat clause (EffectRepeat) into four separate columns. This maps
# directly onto SkillCard's new shape: `effect_condition` and `effect_cost`
# are single, card-level fields (shared by every entry in `effects`), as is
# `effect_repeat`. Only `effects` itself is repeated, to allow a single card
# to perform several actions in order (e.g. "Reroll Black, Reroll Blue", or
# "Gain Green and +4 Fun" which becomes two separate gain effects).
#
# Everything to the right of column S (EffectRepeat) is ignored, per
# instructions -- those are spreadsheet-only EV/helper columns.
# =====================================================================

DIE_ENUM_TYPE = "com.redpup.justsendit.model.Die"

# SkillCardEffectRepeat.matching_die is a plain matchers.Matcher evaluated
# against a rolled die, represented at runtime by:
#
#   message DieRoll {
#     Die die = 1;
#     repeated int32 roll = 2;  // last element is the current value
#   }
#
# To match both color and face value we use a message_matcher with two
# FieldMatchers: one on `die` (color), one on `roll` (value). `roll` is a
# repeated field, and we specifically want the CURRENT value (last element),
# not any historical value -- so this uses collection_matcher's `last`
# variant rather than `any`.
DIE_ROLL_MESSAGE_TYPE = "com.redpup.justsendit.model.player.DieRoll"
DIE_ROLL_COLOR_FIELD = "die"
DIE_ROLL_VALUE_FIELD = "roll"

UNSUPPORTED_EFFECTS = []       # (card_name, EffectValue text)
UNSUPPORTED_CONDITIONS = []    # (card_name, EffectCost/Condition text)
UNSUPPORTED_REPEATS = []       # (card_name, EffectRepeat text)

_SIMPLE_DIE_EFFECT_RE = re.compile(r'^Reroll\s+(Green|Blue|Black|Wild)$')
_GAIN_STAT_RE = re.compile(r'^Gain \+(\d+) (Skill|Fun)$')
_GAIN_DICE_RE = re.compile(r'^Gain (Green|Blue|Black), Gain (Green|Blue|Black)$')
_GAIN_DIE_RE = re.compile(r'^Gain (Green|Blue|Black)$')
_GAIN_DIE_AND_FUN_RE = re.compile(r'^Gain (Green|Blue|Black) and \+(\d+) Fun$')
_REMOVE_DIE_RE = re.compile(r'^Remove (Green|Blue|Black|Wild)$')
_REPEAT_DIE_VALUE_RE = re.compile(r'^(Green|Blue|Black|Wild) (\d+)$')


def _color_matcher_body(color: str, indent: str) -> str:
  """Body of a Matcher matching a specific die color, or any die (Wild)."""
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
  """Builds one full `effects { alter_die { ... reroll ... } }` block."""
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
  """Converts one spreadsheet EffectValue cell into a list of fully-formed
  `effects { ... }` textproto blocks (2-space indented, ready to be embedded
  directly inside a `cards { ... }` block). These blocks contain only the
  action itself -- condition/cost/repeat are card-level and handled
  separately by build_condition_and_cost_blocks() / build_repeat_block()."""
  text = effect_value.strip()
  if not text:
    return []

  # Multiple simple reroll effects joined with a comma, e.g.
  # "Reroll Black, Reroll Blue" each become their own entry, in order.
  parts = [p.strip() for p in text.split(",")]
  if len(parts) > 1 and all(_SIMPLE_DIE_EFFECT_RE.match(p) for p in parts):
    return [_alter_die_effect_block(_SIMPLE_DIE_EFFECT_RE.match(p).group(1).upper())
            for p in parts]

  m = _SIMPLE_DIE_EFFECT_RE.match(text)
  if m:
    return [_alter_die_effect_block(m.group(1).upper())]

  # "Gain <Color> and +N Fun" -- two independent gains, in order.
  m = _GAIN_DIE_AND_FUN_RE.match(text)
  if m:
    color, value = m.group(1).upper(), m.group(2)
    return [
      f"  effects {{\n    gain {{\n      die: DIE_{color}\n    }}\n  }}\n",
      f"  effects {{\n    gain {{\n      points: {value}\n    }}\n  }}\n",
    ]

  # "Gain <Color>, Gain <Color>" -- two independent gains, in order.
  m = _GAIN_DICE_RE.match(text)
  if m:
    color1, color2 = m.group(1).upper(), m.group(2).upper()
    return [
      f"  effects {{\n    gain {{\n      die: DIE_{color1}\n    }}\n  }}\n",
      f"  effects {{\n    gain {{\n      die: DIE_{color2}\n    }}\n  }}\n",
    ]

  # "Gain <Color>" -- gain a die of that color.
  m = _GAIN_DIE_RE.match(text)
  if m:
    color = m.group(1).upper()
    return [f"  effects {{\n    gain {{\n      die: DIE_{color}\n    }}\n  }}\n"]

  # "Gain +N Skill/Fun"
  m = _GAIN_STAT_RE.match(text)
  if m:
    value, kind = m.groups()
    field = "skill" if kind == "Skill" else "points"
    return [f"  effects {{\n    gain {{\n      {field}: {value}\n    }}\n  }}\n"]

  if text == "Gain Fun equal to next card's cost":
    return ["  effects {\n    gain_fun_equal_to_next_card_cost {}\n  }\n"]

  if text == "Gain Fun equal to value rolled":
    return ["  effects {\n    gain_fun_equal_to_value_rolled {}\n  }\n"]

  if text == "Gain tags of this card an additional time":
    return ["  effects {\n    gain_own_tags {}\n  }\n"]

  if text == "Gain tags of card below an additional time":
    return ["  effects {\n    gain_tags_below {}\n  }\n"]

  if text == "Activate the effect of the card below an additional time":
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

  if text == "Trash an additional card":
    return ["  effects {\n    gain {\n      trashes: 1\n    }\n  }\n"]

  if text == "Replenish the shop. You may play additional cards below this.":
    return ["  effects {\n    replenish_shop {}\n  }\n"]

  if text == "At the end of your turn, draw another card from play.":
    return ["  effects {\n    draw_from_play {}\n  }\n"]

  if text == "Move one tile in any direction":
    return ["  effects {\n    move_tile {}\n  }\n"]

  # Nothing matched: this EffectValue has no representation in skill.proto
  # yet. Emit a comment so the generated file still records the intended
  # design, and surface it in the end-of-run summary instead of failing
  # silently.
  UNSUPPORTED_EFFECTS.append((card_name, text))
  return [f'  # TODO(skill.proto): no field represents this effect yet: "{text}"\n']


def build_condition_and_cost_blocks(cond_text: str, own_cost: int,
    card_name: str) -> tuple:
  """Converts the EffectCost/Condition cell into (effect_condition_block,
  effect_cost_block); exactly one of the two (or neither) is non-None."""
  text = cond_text.strip()
  if not text:
    return None, None

  if text == "Success":
    return "  effect_condition {\n    success {}\n  }\n", None

  if text == "If card below costs greater":
    # No explicit threshold is given in the sheet -- interpreted as "the
    # card below costs more than THIS card's own cost", which we already
    # know, so we bake it into the matcher as a literal comparison.
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

  UNSUPPORTED_CONDITIONS.append((card_name, text))
  return f'  # TODO(skill.proto): no condition/cost mapping for "{text}"\n', None


def _die_roll_value_field_block(value: str, indent: str) -> str:
  """Body of a FieldMatcher on DieRoll.roll: matches if `value` equals the
  current (last) rolled value."""
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
  """Converts the EffectRepeat cell into an `effect_repeat { ... }` block."""
  text = repeat_text.strip()
  if not text:
    return None

  if text == "Matching tag on card above":
    return "  effect_repeat {\n    matching_tag_on_cards_above {}\n  }\n"

  m = _REPEAT_DIE_VALUE_RE.match(text)
  if m:
    color, value = m.group(1).upper(), m.group(2)

    # The matcher is always evaluated against a DieRoll{ die, roll } object,
    # so even the color-agnostic "Wild" case has to go through
    # message_matcher -- it just omits the `die` FieldMatcher entirely
    # (an empty AND'ed field list is trivially satisfied for that
    # dimension), rather than matching a bare int.
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

  # Clean out ONLY this specific generated textproto file
  safe_delete_file(target_filepath)

  write_proto_file_header(
    target_filepath,
    "com/redpup/justsendit/model/supply/skill.proto",
    "SkillCardList"
  )

  with open(SKILL_INPUT_CSV, mode='r', newline='', encoding='utf-8') as csv_file:
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

      # Card type: "Starter" File -> starter deck card, "Shop" File -> a
      # purchasable upgrade card.
      file_lower = file_target.lower()
      if file_lower == "starter":
        skill_card_type = "SKILL_CARD_TYPE_STARTER"
      elif file_lower == "shop":
        skill_card_type = "SKILL_CARD_TYPE_UPGRADE"
      else:
        skill_card_type = "SKILL_CARD_TYPE_UNKNOWN"

      # Process dice
      dice_colors = [
        get_safe_cell_value(row, header_map, f"Die {i}").lower()
        for i in range(1, 4)
      ]
      dice_enum_values = [
        f"DIE_{color.upper()}" for color in dice_colors
        if color in ("green", "blue", "black")
      ]

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
        "REST": "EFFECT_CATEGORY_REST",
        "RIDE": "EFFECT_CATEGORY_RIDE",
        "LIFT": "EFFECT_CATEGORY_LIFT",
        "FINALE": "EFFECT_CATEGORY_FINALE",
      }
      proto_category = category_map.get(category_raw, "EFFECT_CATEGORY_UNSET")

      cond_text = get_safe_cell_value(row, header_map, "EffectCost/Condition")
      condition_block, cost_block = build_condition_and_cost_blocks(cond_text, cost, name)

      effect_value_text = get_safe_cell_value(row, header_map, "EffectValue")
      effect_action_blocks = build_effect_action_blocks(effect_value_text, name) if effect_value_text else []

      repeat_text = get_safe_cell_value(row, header_map, "EffectRepeat")
      repeat_block = build_repeat_block(repeat_text, name) if repeat_text else None

      flavor_text = get_safe_cell_value(row, header_map, "FlavorText")

      copies_raw = get_safe_cell_value(row, header_map, "Copies")
      num_copies = int(copies_raw) if copies_raw.isdigit() else 1

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
      card_block += "}\n"

      append_blocks_to_proto(target_filepath, card_block, num_copies)

  if UNSUPPORTED_EFFECTS:
    print("Warning: the following EffectValue phrases have no corresponding "
          "field in skill.proto yet and were emitted as comments only:")
    for card_name, text in UNSUPPORTED_EFFECTS:
      print(f'  - {card_name}: "{text}"')

  if UNSUPPORTED_CONDITIONS:
    print("Warning: the following EffectCost/Condition phrases have no "
          "corresponding field in skill.proto yet and were emitted as "
          "comments only:")
    for card_name, text in UNSUPPORTED_CONDITIONS:
      print(f'  - {card_name}: "{text}"')

  if UNSUPPORTED_REPEATS:
    print("Warning: the following EffectRepeat phrases have no "
          "corresponding mapping and were emitted as comments only:")
    for card_name, text in UNSUPPORTED_REPEATS:
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
