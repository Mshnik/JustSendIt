package com.redpup.justsendit

import com.redpup.justsendit.model.board.grid.HexExtensions.neighbor
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexDirection.HEX_DIRECTION_NORTH_EAST
import com.redpup.justsendit.model.board.hex.proto.HexDirection.HEX_DIRECTION_SOUTH
import com.redpup.justsendit.model.board.hex.proto.hexPoint

/**
 *
 */
fun main() {
  val grid = HexGrid<String>()
  val start = hexPoint {}

  // Setting values
  grid[start] = "Town Center"
  val neighbor = start.neighbor(HEX_DIRECTION_NORTH_EAST)
  grid[neighbor] = "Forest"

  // Getting values
  println("At (0,0): ${grid[start]}")
  println("At neighbor (1,-1): ${grid[neighbor]}")

  // Moving around
  var current = start
  repeat(3) {
    current = current.neighbor(HEX_DIRECTION_SOUTH)
    grid[current] = "Path segment"
  }
}