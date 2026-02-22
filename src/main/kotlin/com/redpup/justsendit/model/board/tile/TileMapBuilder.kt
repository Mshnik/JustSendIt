package com.redpup.justsendit.model.board.tile

import com.google.inject.Inject
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation
import com.redpup.justsendit.model.supply.TileSupply

/** Utility to build the tile map from the [TileSupply]. */
class TileMapBuilder @Inject constructor(private val tileSupply: TileSupply) {
  /** Builds a hex grid map from a TileReader. */
  fun build(): HexGrid<MountainTile> {
    val grid = HexGrid<MountainTile>()
    val slopesByGrade = tileSupply.tiles.filter { it.hasSlope() }.groupBy { it.slope.grade }
      .mapValues { it.value.shuffled().toMutableList() }
    val liftsByColorAndDirection =
      tileSupply.tiles.filter { it.hasLift() }.groupBy { Pair(it.lift.color, it.lift.direction) }
        .mapValues {
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

    for (location in tileSupply.locations) {
      grid[location.point] = pickTile(location)
    }
    return grid
  }
}
