package com.redpup.justsendit.model.board.tile

import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation

/** Functions to build the tile map. */
object TileMap {

  /** Builds a hex grid map from a TileReader. */
  fun TileReader.toMap(): HexGrid<MountainTile> {
    val grid = HexGrid<MountainTile>()
    val slopesByGrade =
      getTilesList().filter { it.hasSlope() }.groupBy { it.slope.grade }
        .mapValues { it.value.toMutableList() }
    val liftsByColorAndDirection =
      getTilesList().filter { it.hasLift() }
        .groupBy { Pair(it.lift.color, it.lift.direction) }
        .mapValues {
          check(it.value.size == 1)
          it.value.first()
        }
        .toMutableMap()

    /** Picks, returns, and removes a random tile matching [location]'s specifications. */
    fun pickTile(location: MountainTileLocation): MountainTile = when (location.contentCase) {
      MountainTileLocation.ContentCase.GRADE -> {
        val gradeList = slopesByGrade[location.grade] ?: throw IllegalArgumentException()
        gradeList.removeAt(gradeList.indices.random())
      }

      MountainTileLocation.ContentCase.LIFT -> liftsByColorAndDirection.remove(
        Pair(
          location.lift.color,
          location.lift.direction
        )
      )
        ?: throw IllegalArgumentException("No lift tile found with parameters ${location.lift} in $liftsByColorAndDirection")

      MountainTileLocation.ContentCase.CONTENT_NOT_SET -> throw IllegalArgumentException()
    }

    for (location in getTileLocationsList()) {
      grid[location.point] = pickTile(location)
    }
    return grid
  }
}
