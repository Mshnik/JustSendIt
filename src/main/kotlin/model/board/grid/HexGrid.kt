package com.redpup.justsendit.model.board.grid

import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.hex.proto.HexPoint

/**
 * A generic hexagonal grid that stores elements of type T.
 * The grid grows dynamically as elements are added.
 */
class HexGrid<T> : Iterable<Pair<HexPoint, T>> {
  private val cells = mutableMapOf<HexPoint, T>()

  /**
   * Retrieves the element at the given [point].
   * Returns null if no element exists at that coordinate.
   */
  operator fun get(point: HexPoint): T? = cells[point]

  /** Stores an element at the given [point]. */
  operator fun set(point: HexPoint, value: T) {
    cells[point] = value
  }

  /** Clears cells. */
  fun clear() = cells.clear()

  /** Returns the total number of occupied hexes in the grid. */
  fun size(): Int = cells.size

  /** Checks if a specific point has an assigned value. */
  fun contains(point: HexPoint): Boolean = cells.containsKey(point)

  /** Returns all points currently stored in the grid. */
  fun keys(): Set<HexPoint> = cells.keys

  /** Returns all values currently stored in the grid. */
  fun values(): Collection<T> = cells.values

  /** Returns iterable access to this board. */
  override fun iterator(): Iterator<Pair<HexPoint, T>> =
    cells.asSequence().map { Pair(it.key, it.value) }.iterator()

  /** Returns the 2d x,y grid bounds of this hex grid. */
  fun bounds(): Bounds {
    if (keys().isEmpty()) {
      return Bounds(0.0, 0.0, 0.0, 0.0)
    } else {
      return Bounds(keys().minOf { it.toX() }, keys().minOf { it.toY() },
                    keys().maxOf { it.toX() }, keys().maxOf { it.toY() })
    }
  }
}

/** 2D x,y bounds of a grid. */
data class Bounds(val minX: Double, val minY: Double, val maxX: Double, val maxY: Double) {
  val height = maxY - minY + 1.0
  val width = maxX - minX + 1.0
}
