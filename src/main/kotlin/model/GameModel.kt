package com.redpup.justsendit.model

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.PlayerCardList
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Immutable access to game model. */
interface GameModel {
  /** The mountain map. */
  val tileMap: HexGrid<MountainTile>

  /** Immutable access to all players. */
  val players: List<Player>

  /**Immutable access to the clock. */
  val clock: Clock
}

/** Top level joined game model state. */
class MutableGameModel(
  private val tilesPath: String = "src/main/resources/model/board/tile/tiles.textproto",
  private val locationsPath: String = "src/main/resources/model/board/tile/tile_locations.textproto",
  private val playersPath: String = "src/main/resources/model/players/players.textproto",
  val playerCount: Int = 4,
  val skillDecks: SkillDecks,
) : GameModel {
  override val tileMap: HexGrid<MountainTile> =
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
  override val players: MutableList<MutablePlayer> =
    TextProtoReaderImpl(
      playersPath, PlayerCardList::newBuilder,
      PlayerCardList.Builder::getPlayerList, shuffle = true
    ).invoke()
      .shuffled()
      .subList(0, playerCount)
      .map { MutablePlayer(it) }
      .toMutableList()
  override val clock = MutableClock()

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

/** Recording of time in game. */
interface Clock {
  /** What turn of day it is. */
  val turn: Int

  /** What game of day it is. */
  val day: Int
}

class MutableClock(override var turn: Int = 0, override var day: Int = 0) : Clock {
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