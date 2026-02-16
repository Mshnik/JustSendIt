package com.redpup.justsendit.model

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.model.player.BasicPlayerHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
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
      .map { MutablePlayer(it, BasicPlayerHandler()) }
      .toMutableList()
  override val clock = MutableClock()

  init {
    for (player in players) {
      player.buyStartingDeck(skillDecks)
      player.location = HexPoint(0, 0)
    }
  }

  /** Executes one turn for all players. Returns true if the day is now over, false otherwise. */
  private fun turn(): Boolean {
    for (player in players) {
      do {
        val decision = player.handler.makeMountainDecision(player, this)
        val continueTurn = executeDecision(player, decision)
      } while (continueTurn)
    }
    if (clock.turn < Clock.Params.MAX_TURN) {
      clock.next()
      return false
    } else {
      return true
    }
  }

  /**
   * Sorts players in ascending order of points.
   * Returns true if the game is over, false otherwise.
   */
  private fun cleanup(): Boolean {
    players.sortBy { it.points }
    if (clock.day < Clock.Params.MAX_DAY) {
      clock.nextDay()
      return false
    } else {
      return true
    }
  }

  /**
   * Executes the given [decision] for the given [player]. Returns true if the
   * player's turn continues, false if their turn is now over.
   */
  private fun executeDecision(player: Player, decision: MountainDecision): Boolean {
    return when (decision.decisionCase) {
      MountainDecision.DecisionCase.SKI_RIDE -> executeSkiRide(player, decision.skiRide)
      MountainDecision.DecisionCase.REST -> {
        executeRest(player)
        false
      }

      MountainDecision.DecisionCase.LIFT -> {
        executeLift(player)
        false
      }

      MountainDecision.DecisionCase.PASS -> false
      MountainDecision.DecisionCase.EXIT -> {
        executeExit(player)
        false
      }

      MountainDecision.DecisionCase.DECISION_NOT_SET -> throw IllegalArgumentException()
    }
  }

  private fun executeSkiRide(player: Player, hexDirection: HexDirection): Boolean = TODO()
  private fun executeRest(player: Player): Unit = TODO()
  private fun executeLift(player: Player): Unit = TODO()
  private fun executeExit(player: Player): Unit = TODO()
}

/** Recording of time in game. */
interface Clock {
  /** What turn of day it is. */
  val turn: Int

  /** What game of day it is. */
  val day: Int

  object Params {
    const val MAX_TURN = 8
    const val MAX_DAY = 3
  }
}

class MutableClock(override var turn: Int = 1, override var day: Int = 1) : Clock {
  /** Advances to the next turn. */
  fun next() {
    turn++
  }

  /** Advances to the next day. */
  fun nextDay() {
    turn = 1
    day++
  }
}