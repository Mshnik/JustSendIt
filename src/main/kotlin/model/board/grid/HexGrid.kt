package com.redpup.justsendit.model.board.grid

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

  /** Returns the height of the grid in hexes. */
  fun height(): Double =
    if (keys().isEmpty()) 0.0 else 1 + keys().maxOf { it.r + it.q / 2.0 } - keys().minOf { it.r + it.q / 2.0 }

  /** Returns the width of the grid in hexes. */
  fun width(): Int = if (keys().isEmpty()) 0 else 1 + keys().maxOf { it.q } - keys().minOf { it.q }
}
