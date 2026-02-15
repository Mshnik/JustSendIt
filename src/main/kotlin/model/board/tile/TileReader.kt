package com.redpup.justsendit.model.board.tile

import com.google.protobuf.TextFormat
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocation
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import java.nio.file.Files
import java.nio.file.Path

/** Access to file list of tiles. */
interface TileReader {
  /** Returns the list of all tiles in the box. Not all will be used. */
  fun getTilesList(): List<MountainTile>

  /** Returns the list of locations to fill on the mountain board. */
  fun getTileLocationsList(): List<MountainTileLocation>
}

/** Impl of [TileReader]. */
class TileReaderImpl(
  private val tilesPath: String,
  private val tileLocationsPath: String,
) : TileReader {
  private val tiles: List<MountainTile> by lazy {
    val builder = MountainTileList.newBuilder()
    TextFormat.merge(Files.readString(Path.of(tilesPath)), builder)
    builder.build().tilesList
  }

  private val tileLocations: List<MountainTileLocation> by lazy {
    val builder = MountainTileLocationList.newBuilder()
    TextFormat.merge(Files.readString(Path.of(tileLocationsPath)), builder)
    builder.build().locationList
  }

  override fun getTilesList(): List<MountainTile> = tiles

  override fun getTileLocationsList(): List<MountainTileLocation> = tileLocations
}

