package com.redpup.justsendit.model.board.grid

import kotlin.math.abs

/**
 * Represents a point in a 2D hexagonal grid using Axial Coordinates (q, r).
 */
data class HexPoint(val q: Int, val r: Int) {

  /**
   * Returns a new HexPoint adjacent to this one in the specified direction.
   */
  fun neighbor(direction: HexDirection): HexPoint {
    return HexPoint(q + direction.dq, r + direction.dr)
  }

  /**
   * Calculates the Manhattan-style distance between two hexes.
   */
  fun distanceTo(other: HexPoint): Int {
    return (
      abs(q - other.q)
        + abs(q + r - other.q - other.r)
        + abs(r - other.r)
      ) / 2
  }
}

/**
 * Defines the six possible directions in a "Pointy-Top" hexagonal grid.
 */
enum class HexDirection(val dq: Int, val dr: Int) {
  NORTH(0, -1),
  NORTH_EAST(1, -1),
  SOUTH_EAST(1, 0),
  SOUTH(0, 1),
  SOUTH_WEST(-1, 1),
  NORTH_WEST(-1, 0)
}
