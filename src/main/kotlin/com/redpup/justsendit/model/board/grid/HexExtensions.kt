package com.redpup.justsendit.model.board.grid

import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import kotlin.math.abs
import kotlin.math.sqrt

/** Extensions on hex protos. */
object HexExtensions {

  /** Returns the delta in the q direction of this direction. */
  val HexDirection.dq
    get() = when (this) {
      HexDirection.HEX_DIRECTION_NORTH -> 0
      HexDirection.HEX_DIRECTION_NORTH_EAST -> 1
      HexDirection.HEX_DIRECTION_SOUTH_EAST -> 1
      HexDirection.HEX_DIRECTION_SOUTH -> 0
      HexDirection.HEX_DIRECTION_SOUTH_WEST -> -1
      HexDirection.HEX_DIRECTION_NORTH_WEST -> -1
      HexDirection.HEX_DIRECTION_UNSET, HexDirection.UNRECOGNIZED -> throw IllegalArgumentException()
    }

  /** Returns the delta in the q direction of this direction. */
  val HexDirection.dr
    get() = when (this) {
      HexDirection.HEX_DIRECTION_NORTH -> -1
      HexDirection.HEX_DIRECTION_NORTH_EAST -> -1
      HexDirection.HEX_DIRECTION_SOUTH_EAST -> 0
      HexDirection.HEX_DIRECTION_SOUTH -> 1
      HexDirection.HEX_DIRECTION_SOUTH_WEST -> 1
      HexDirection.HEX_DIRECTION_NORTH_WEST -> 0
      HexDirection.HEX_DIRECTION_UNSET, HexDirection.UNRECOGNIZED -> throw IllegalArgumentException()
    }

  /** Returns true iff the given direction is down-mountain. */
  val HexDirection.isDownMountain
    get() = when (this) {
      HexDirection.HEX_DIRECTION_SOUTH, HexDirection.HEX_DIRECTION_SOUTH_EAST, HexDirection.HEX_DIRECTION_SOUTH_WEST -> true
      HexDirection.HEX_DIRECTION_NORTH, HexDirection.HEX_DIRECTION_NORTH_EAST, HexDirection.HEX_DIRECTION_NORTH_WEST -> false
      HexDirection.HEX_DIRECTION_UNSET, HexDirection.UNRECOGNIZED -> throw IllegalArgumentException()
    }

  /** Constructs a [HexPoint] with the given args. */
  fun Pair<Int, Int>.toHexPoint() = hexPoint {
    this.q = first
    this.r = second
  }

  /** Constructs a [HexPoint] with the given args. */
  fun createHexPoint(q: Int, r: Int) = Pair(q, r).toHexPoint()

  /**
   * Returns a new HexPoint adjacent to this one in the specified direction.
   */
  operator fun HexPoint.plus(direction: HexDirection): HexPoint {
    return hexPoint {
      q = this@plus.q + direction.dq
      r = this@plus.r + direction.dr
    }
  }

  /**
   * Calculates the Manhattan-style distance between two hexes.
   */
  fun HexPoint.distanceTo(other: HexPoint): Int {
    return (abs(q - other.q) + abs(q + r - other.q - other.r) + abs(r - other.r)) / 2
  }

  /** Converts this hex point to x in x,y space. */
  fun HexPoint.toX() = q * 1.5

  private val SQRT_3 = sqrt(3.0)

  /** Converts this hex point to y in x,y space. */
  fun HexPoint.toY() = (r + q / 2.0) * SQRT_3
}
