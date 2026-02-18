package com.redpup.justsendit.model

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.LiftColor
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.model.player.BasicPlayerHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecision.SkiRideDecision
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
  tilesPath: String = "src/main/resources/model/board/tile/tiles.textproto",
  locationsPath: String = "src/main/resources/model/board/tile/tile_locations.textproto",
  playersPath: String = "src/main/resources/model/players/players.textproto",
  playerHandlers: List<PlayerHandler> = List(4) { BasicPlayerHandler() },
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
  private val lifts =
    tileMap.entries().filter { it.value.hasLift() }
      .groupBy { it.value.lift.color }

  override val players: List<MutablePlayer> =
    TextProtoReaderImpl(
      playersPath, PlayerCardList::newBuilder,
      PlayerCardList.Builder::getPlayerList, shuffle = true
    ).invoke()
      .shuffled()
      .subList(0, playerHandlers.size)
      .mapIndexed { index, card -> MutablePlayer(card, playerHandlers[index]) }
      .toMutableList()
  private val playerOrder = MutableList(players.size) { it }
  override val clock = MutableClock()

  init {
    for (player in players) {
      player.buyStartingDeck(skillDecks)
      player.location = HexPoint(0, 0)
    }
  }

  /** Returns the players in turn order. */
  private fun playersInTurnOrder() = playerOrder.map { players[it] }

  /** Executes one turn for all players. Returns true if the day is now over, false otherwise. */
  fun turn(): Boolean {
    for (player in playersInTurnOrder()) {
      if (player.isOnMountain) {
        var subTurn = 0
        do {
          val decision = player.handler.makeMountainDecision(player, this)
          val continueTurn = executeDecision(player, decision, subTurn)
          subTurn++
        } while (continueTurn)
        player.ingestTurn()
      }
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
  fun cleanup(): Boolean {
    playerOrder.sortBy { players[it].points }
    if (clock.day < Clock.Params.MAX_DAY) {
      clock.nextDay()
      for (player in playersInTurnOrder()) {
        player.location = player.handler.getStartingLocation(player, this)
      }
      return false
    } else {
      return true
    }
  }

  /**
   * Executes the given [decision] for the given [player]. Returns true if the
   * player's turn continues, false if their turn is now over.
   */
  private fun executeDecision(
    player: MutablePlayer,
    decision: MountainDecision,
    subTurn: Int,
  ): Boolean {
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

      MountainDecision.DecisionCase.PASS -> {
        check(subTurn > 0) { "Can't pass without taking at least one action" }
        false
      }

      MountainDecision.DecisionCase.EXIT -> {
        executeExit(player)
        false
      }

      MountainDecision.DecisionCase.DECISION_NOT_SET -> throw IllegalArgumentException()
    }
  }

  private fun executeSkiRide(player: MutablePlayer, skiRideDecision: SkiRideDecision): Boolean {
    // Check directions of ski/ride, make sure we don't go off mountain.
    val location = player.location
    check(location != null) { "Player is off-map." }
    check(skiRideDecision.direction.isDownMountain) { "Can only ski/ride down mountain, found ${skiRideDecision.direction}" }
    val destination = location + skiRideDecision.direction
    val destinationTile = tileMap[destination]
    check(destinationTile != null) { "Destination is invalid: $destination" }

    // If destination has lift, player just goes there.
    if (destinationTile.hasLift()) {
      player.location = destination
      check(skiRideDecision.numCards == 0) {
        "Must play 0 cards to ski/ride onto lift, got ${skiRideDecision.numCards}"
      }
      return true
    }

    // Check number of cards played.
    check(skiRideDecision.numCards >= 1 && skiRideDecision.numCards <= clock.day) {
      "Must play between [1,${clock.day}] cards, got ${skiRideDecision.numCards}"
    }
    check(skiRideDecision.numCards <= player.skillDeck.size) {
      "Only ${player.skillDeck.size} cards remaining, cannot play ${skiRideDecision.numCards} cards"
    }

    // Actually play cards and compare to difficulty.
    val skill =
      (1..skiRideDecision.numCards).sumOf { player.playSkillCard()!! } + player.computeBonus(
        destinationTile.slope
      )
    val difficulty =
      destinationTile.slope.difficulty + player.turn.speed * SPEED_DIFFICULTY_MODIFIER

    // Compute and apply result to turn.
    val success = skill >= difficulty
    if (success) {
      player.turn.points += difficulty
      player.turn.speed++
    }
    if (skill <= difficulty && skill >= difficulty.toDouble() / 2.0) {
      player.turn.experience++
    }

    // Turn continues if successful.
    return success
  }

  /** Executes the player taking a rest. */
  private fun executeRest(player: MutablePlayer) {
    player.refreshSkillDeck()
  }

  /** Executes the player taking a lift. */
  private fun executeLift(player: MutablePlayer) {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val tile = tileMap[location]!!
    check(tile.hasLift()) { "Location $location does not have a lift" }
    player.location = getOtherLiftLocation(tile.lift.color, location)
    player.refreshSkillDeck()
  }

  /** Returns the matching lift location for [color] that is not [location]. */
  private fun getOtherLiftLocation(color: LiftColor, location: HexPoint): HexPoint =
    lifts[color]!!.find { it.key != location }!!.key

  /** Executes the player leaving the mountain. */
  private fun executeExit(player: MutablePlayer) {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val tile = tileMap[location]!!
    check(tile.apresLink > 0) { "Location $location does not have an exit" }

    player.location = null
    // TODO: Claim apres reward.
  }

  companion object {
    /** Multiplier of speed to difficulty. */
    const val SPEED_DIFFICULTY_MODIFIER = 2
  }
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