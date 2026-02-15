package com.redpup.justsendit.model.board.grid

import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import kotlin.math.abs

/** Extensions on hex protos. */
object HexExtensions {

  /** Returns the delta in the q direction of this direction. */
  fun HexDirection.dq() = when (this) {
    HexDirection.HEX_DIRECTION_NORTH -> 0
    HexDirection.HEX_DIRECTION_NORTH_EAST -> 1
    HexDirection.HEX_DIRECTION_SOUTH_EAST -> 1
    HexDirection.HEX_DIRECTION_SOUTH -> 0
    HexDirection.HEX_DIRECTION_SOUTH_WEST -> -1
    HexDirection.HEX_DIRECTION_NORTH_WEST -> -1
    HexDirection.HEX_DIRECTION_UNSET -> throw IllegalArgumentException()
    HexDirection.UNRECOGNIZED -> throw IllegalArgumentException()
  }

  /** Returns the delta in the q direction of this direction. */
  fun HexDirection.dr() = when (this) {
    HexDirection.HEX_DIRECTION_NORTH -> -1
    HexDirection.HEX_DIRECTION_NORTH_EAST -> -1
    HexDirection.HEX_DIRECTION_SOUTH_EAST -> 0
    HexDirection.HEX_DIRECTION_SOUTH -> 1
    HexDirection.HEX_DIRECTION_SOUTH_WEST -> 1
    HexDirection.HEX_DIRECTION_NORTH_WEST -> 0
    HexDirection.HEX_DIRECTION_UNSET -> throw IllegalArgumentException()
    HexDirection.UNRECOGNIZED -> throw IllegalArgumentException()
  }

  /** Constructs a [HexPoint] with the given args. */
  fun HexPoint(q: Int, r: Int) = hexPoint {
    this.q = q
    this.r = r
  }

  /**
   * Returns a new HexPoint adjacent to this one in the specified direction.
   */
  fun HexPoint.neighbor(direction: HexDirection): HexPoint {
    return hexPoint {
      q = this@neighbor.q + direction.dq()
      r = this@neighbor.r + direction.dr()
    }
  }

  /**
   * Calculates the Manhattan-style distance between two hexes.
   */
  fun HexPoint.distanceTo(other: HexPoint): Int {
    return (
      abs(q - other.q)
        + abs(q + r - other.q - other.r)
        + abs(r - other.r)
      ) / 2
  }
}
