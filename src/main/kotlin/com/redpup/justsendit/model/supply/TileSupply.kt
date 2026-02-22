package com.redpup.justsendit.model.supply

import com.google.inject.Inject
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.util.TextProtoReaderImpl
import javax.inject.Qualifier
import javax.inject.Singleton

/** Access to the mountain tiles supply. */
interface TileSupply {
  /** Returns the list of tiles in the supply. */
  val tiles: List<MountainTile>

  /** Returns the list of locations in the supply. */
  val locations: List<MountainTileLocation>
}

/** [Qualifier] for the path of the tile supply. */
@Qualifier
annotation class TilePath

/** [Qualifier] for the path of the tile location supply. */
@Qualifier
annotation class LocationPath

/** Implementation of [TileSupply]. */
@Singleton
class TileSupplyImpl @Inject constructor(
  @TilePath tilePath: String,
  @LocationPath locationPath: String,
) : TileSupply {
  private val tileReader = TextProtoReaderImpl(
    tilePath,
    MountainTileList::newBuilder,
    MountainTileList.Builder::getTilesList,
    shuffle = true
  )
  private val locationReader = TextProtoReaderImpl(
    locationPath,
    MountainTileLocationList::newBuilder,
    MountainTileLocationList.Builder::getLocationList
  )

  override val tiles = tileReader()
  override val locations = locationReader()
}