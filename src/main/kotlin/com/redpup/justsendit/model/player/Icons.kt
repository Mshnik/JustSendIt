package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.proto.Icon
import com.redpup.justsendit.model.player.proto.Icon.TypeCase

/**
 * Utilities for operating on [Icon]s.
 */
object Icons {
  /** Returns true iff [this] matches [SlopeTile]. */
  fun Icon.matches(slope: SlopeTile): Boolean = when (typeCase) {
    TypeCase.GRADE -> grade == slope.grade
    TypeCase.CONDITION -> condition == slope.condition
    TypeCase.HAZARD -> hazard in slope.hazardsList
    TypeCase.WILD -> wild
    else -> false
  }
}