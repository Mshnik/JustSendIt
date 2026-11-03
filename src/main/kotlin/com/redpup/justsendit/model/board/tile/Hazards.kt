package com.redpup.justsendit.model.board.tile

import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.HazardType

/** Utilities for working with [Hazard] and [HazardType]. */
object Hazards {
  /** Returns the type of this [Hazard]. */
  fun Hazard.type(): HazardType = HazardType.forNumber(hazardCase.number)
}