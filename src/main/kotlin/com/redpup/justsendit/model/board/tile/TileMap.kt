package com.redpup.justsendit.model.board.tile

import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation
import com.redpup.justsendit.util.TextProtoReader

/** Functions to build the tile map. */
object TileMap {

  /** Builds a hex grid map from a TileReader. */
  fun constructMap(
    tiles: TextProtoReader<MountainTile>,
    locations: TextProtoReader<MountainTileLocation>,
  ): HexGrid<MountainTile> {
    val grid = HexGrid<MountainTile>()
    val slopesByGrade = tiles().filter { it.hasSlope() }.groupBy { it.slope.grade }
      .mapValues { it.value.shuffled().toMutableList() }
    val liftsByColorAndDirection =
      tiles().filter { it.hasLift() }.groupBy { Pair(it.lift.color, it.lift.direction) }.mapValues {
          check(it.value.size == 1)
          it.value.first()
        }.toMutableMap()

    /** Picks, returns, and removes a random tile matching [location]'s specifications. */
    fun pickTile(location: MountainTileLocation): MountainTile = when (location.contentCase) {
      MountainTileLocation.ContentCase.GRADE -> slopesByGrade[location.grade]?.removeFirst()
        ?: throw IllegalArgumentException("No slope tile found with grade ${location.grade} in $slopesByGrade")

      MountainTileLocation.ContentCase.LIFT -> liftsByColorAndDirection.remove(
        Pair(
          location.lift.color, location.lift.direction
        )
      )
        ?: throw IllegalArgumentException("No lift tile found with parameters ${location.lift} in $liftsByColorAndDirection")

      MountainTileLocation.ContentCase.CONTENT_NOT_SET -> throw IllegalArgumentException()
    }

    for (location in locations()) {
      grid[location.point] = pickTile(location)
    }
    return grid
  }
}
