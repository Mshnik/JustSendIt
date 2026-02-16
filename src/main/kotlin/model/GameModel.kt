package com.redpup.justsendit.model

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.PlayerCardList
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Top level joined game model state. */
class GameModel(
  private val tilesPath: String = "src/main/resources/model/board/tile/tiles.textproto",
  private val locationsPath: String = "src/main/resources/model/board/tile/tile_locations.textproto",
  private val playersPath: String = "src/main/resources/model/player/players.textproto",
  val playerCount: Int = 4,
  val skillDecks: SkillDecks,
) {
  val tileMap: HexGrid<MountainTile> =
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
  val players: MutableList<MutablePlayer> =
    TextProtoReaderImpl(
      playersPath, PlayerCardList::newBuilder,
      PlayerCardList.Builder::getPlayerList, shuffle = true
    ).invoke()
      .shuffled()
      .subList(0, playerCount)
      .map { MutablePlayer(it) }
      .toMutableList()
  val clock = Clock()

  init {
    for (player in players) {
      player.buyStartingDeck(skillDecks)
      player.location = HexPoint(0, 0)
    }
  }

  /** Sorts players in ascending order of points. */
  private fun sortPlayers() {
    players.sortBy { it.points }
  }
}

class Clock(var turn: Int = 0, var day: Int = 0) {
  /** Advances to the next turn. */
  fun next() {
    turn++
  }

  /** Advances to the next day. */
  fun nextDay() {
    turn = 0
    day++
  }
}