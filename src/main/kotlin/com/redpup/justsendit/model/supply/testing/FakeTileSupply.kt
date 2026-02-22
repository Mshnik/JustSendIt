package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation
import com.redpup.justsendit.model.supply.TileSupply
import javax.inject.Inject
import javax.inject.Singleton


/** Testing instance of [TileSupply]. */
@VisibleForTesting
@Singleton
class FakeTileSupply @Inject constructor() : TileSupply {
  /** Resets this FakeTileSupply to the default state. */
  fun reset() {
    tiles = listOf()
    locations = listOf()
  }

  override var tiles = listOf<MountainTile>()
  override var locations = listOf<MountainTileLocation>()
}