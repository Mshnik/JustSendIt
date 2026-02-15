package com.redpup.justsendit

import com.redpup.justsendit.model.board.grid.HexDirection
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.grid.HexPoint

/**
 *
 */
fun main() {
  val grid = HexGrid<String>()
  val start = HexPoint(0, 0)

  // Setting values
  grid[start] = "Town Center"
  val neighbor = start.neighbor(HexDirection.NORTH_EAST)
  grid[neighbor] = "Forest"

  // Getting values
  println("At (0,0): ${grid[start]}")
  println("At neighbor (1,-1): ${grid[neighbor]}")

  // Moving around
  var current = start
  repeat(3) {
    current = current.neighbor(HexDirection.SOUTH)
    grid[current] = "Path segment"
  }
}