package com.redpup.justsendit.model

import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Top level joined game model state. */
class GameModel(
  private val tilesPath: String = "src/main/resources/model/board/tile/tiles.textproto",
  private val locationsPath: String = "src/main/resources/model/board/tile/tile_locations.textproto",
) {
  val tileMap: HexGrid<MountainTile> by lazy {
    constructMap(
      TextProtoReaderImpl(
        tilesPath,
        MountainTileList::newBuilder,
        MountainTileList.Builder::getTilesList,
        shuffle = true
      ),
      TextProtoReaderImpl(
        locationsPath,
        MountainTileLocationList::newBuilder,
        MountainTileLocationList.Builder::getLocationList
      ),
    )
  }
}