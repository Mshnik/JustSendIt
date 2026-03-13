package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Condition.*
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.Hazard.*
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.proto.TrainingChip
import com.redpup.justsendit.model.player.proto.TrainingChip.TypeCase.*

/** Returns true iff [this] applies to [tile]. */
fun TrainingChip.appliesTo(tile: SlopeTile) = when (typeCase) {
  CONDITION -> condition == tile.condition
  HAZARD -> hazard in tile.hazardsList
  WILD -> true
  TYPE_NOT_SET, null -> throw IllegalArgumentException()
}

/** Gives the value of this training chip when the bonus applies. */
fun TrainingChip.value() = when (typeCase) {
  CONDITION -> when (condition) {
    CONDITION_GROOMED -> 3
    CONDITION_POWDER -> 3
    CONDITION_ICY -> 4
    CONDITION_UNSET, Condition.UNRECOGNIZED, null -> throw IllegalArgumentException()
  }

  HAZARD -> when (hazard) {
    HAZARD_MOGULS -> 4
    HAZARD_TREES -> 5
    HAZARD_CLIFFS -> 5
    HAZARD_UNSET, Hazard.UNRECOGNIZED, null -> throw IllegalArgumentException()
  }

  WILD -> 2
  TYPE_NOT_SET, null -> throw IllegalArgumentException()
}