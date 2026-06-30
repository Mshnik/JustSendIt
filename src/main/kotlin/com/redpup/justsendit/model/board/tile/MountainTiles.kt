package com.redpup.justsendit.model.board.tile

import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.supply.proto.Icon
import com.redpup.justsendit.model.supply.proto.Icon.TypeCase

/** Extension functions for working with [MountainTile]s and its wrapped types. */
object MountainTiles {
  /** Returns true iff [this] has [condition]. */
  infix fun SlopeTile.has(condition: Condition) = condition == this.condition

  /** Returns true iff [hazard] is in [this]. */
  infix fun SlopeTile.has(hazard: Hazard) = hazard in hazardsList

  /** Returns true iff [this] matches [SlopeTile]. */
  fun Icon.matches(slope: SlopeTile): Boolean = when (typeCase) {
    TypeCase.GRADE -> grade == slope.grade
    TypeCase.CONDITION -> condition == slope.condition
    TypeCase.HAZARD -> hazard in slope.hazardsList
    TypeCase.WILD -> wild
    else -> false
  }

  /** Applies [hazard] to a given skill roll, returning the updated value. */
  fun Int.applyHazard(hazard: Hazard): Int = when (hazard) {
    Hazard.HAZARD_UNSET, Hazard.UNRECOGNIZED, Hazard.HAZARD_MOGULS -> this
    Hazard.HAZARD_TREES -> if (this == 5) 0 else this
    Hazard.HAZARD_CLIFFS -> if (this == 2 || this == 3) 0 else this
  }

  /** Applies [hazards] to a given skill roll in order, returning the updated value. */
  fun Int.applyHazards(hazards: List<Hazard>): Int = if (hazards.isEmpty()) this else {
    var value = this
    for (hazard in hazards) {
      value = value.applyHazard(hazard)
    }
    value
  }

}