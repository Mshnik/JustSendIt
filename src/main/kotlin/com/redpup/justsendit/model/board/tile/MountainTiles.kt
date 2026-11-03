package com.redpup.justsendit.model.board.tile

import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.HazardType
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.supply.proto.Icon
import com.redpup.justsendit.model.supply.proto.Icon.TypeCase

/** Extension functions for working with [MountainTile]s and its wrapped types. */
object MountainTiles {
  /** Returns true iff [this] has [condition]. */
  infix fun MountainTile.has(condition: Condition) = condition == this.condition

  /** Returns true iff [hazard] is in [this]. */
  infix fun MountainTile.has(hazard: HazardType) = this.hazard.hazardCase.number == hazard.number

  /** Returns true iff [this] matches [MountainTile]. */
  fun Icon.matches(slope: MountainTile): Boolean = when (typeCase) {
    TypeCase.GRADE -> grade == slope.grade
    TypeCase.CONDITION -> condition == slope.condition
    TypeCase.HAZARD -> slope.has(hazard)
    TypeCase.WILD -> wild
    else -> false
  }

  /** Applies [hazard] to a given skill roll, returning the updated value. */
  fun Int.applyHazard(hazard: Hazard): Int = when (hazard) {
    // TODO: Fix.
    // Hazard.HAZARD_UNSET, Hazard.UNRECOGNIZED, Hazard.HAZARD_MOGULS -> this
    // Hazard.HAZARD_TREES -> if (this == 5) 0 else this
    // Hazard.HAZARD_CLIFFS -> if (this == 2 || this == 3) 0 else thi
    else -> this
  }

}