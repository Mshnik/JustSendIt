package com.redpup.justsendit.model

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.ApresFactoryImpl
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.TileMap.constructMap
import com.redpup.justsendit.model.board.tile.proto.LiftColor
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.board.tile.proto.MountainTileLocationList
import com.redpup.justsendit.model.player.*
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecision.SkiRideDecision
import com.redpup.justsendit.model.player.proto.PlayerCardList
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.ApresDeckImpl
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Immutable access to game model. */
interface GameModel {
  /** Mutates this game model with the given fn. */
  fun mutate(fn: MutableGameModel.() -> Unit)

  /** The mountain map. */
  val tileMap: HexGrid<MountainTile>

  /** Immutable access to all players. */
  val players: List<Player>

  /** The apres cards that are available at each location. Changes each day. */
  val apres: List<Apres>

  /**Immutable access to the clock. */
  val clock: Clock

  /** The skill decks in the game. */
  val skillDecks: SkillDecks
}

/** Top level joined game model state. */
class MutableGameModel(
  tilesPath: String = "src/main/resources/com/redpup/justsendit/model/board/tile/tiles.textproto",
  locationsPath: String = "src/main/resources/com/redpup/justsendit/model/board/tile/tile_locations.textproto",
  playersPath: String = "src/main/resources/com/redpup/justsendit/model/players/players.textproto",
  apresPath: String = "src/main/resources/com/redpup/justsendit/model/apres/apres.textproto",
  playerHandlers: List<PlayerHandler> = List(4) { BasicPlayerHandler() },
  apresFactory: ApresFactory = ApresFactoryImpl,
  playerFactory: PlayerFactory = PlayerFactoryImpl,
  override val skillDecks: SkillDecks,
) : GameModel {
  /** Applies fn to this. */
  override fun mutate(fn: MutableGameModel.() -> Unit) {
    this.fn()
  }

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

  private val apresDeck: ApresDeck = ApresDeckImpl(apresPath, apresFactory)
  override val apres: MutableList<Apres> = mutableListOf()

  override val players: List<MutablePlayer> =
    TextProtoReaderImpl(
      playersPath, PlayerCardList::newBuilder,
      PlayerCardList.Builder::getPlayerList, shuffle = true
    ).invoke()
      .shuffled()
      .subList(0, playerHandlers.size)
      .mapIndexed { index, card -> playerFactory.create(card, playerHandlers[index]) }

  private val playerOrder = MutableList(players.size) { it }
  override val clock = MutableClock()

  init {
    for (player in players) {
      player.buyStartingDeck(skillDecks)
      player.location = createHexPoint(0, 0)
    }
    apresDeck.reset()
    populateApresSlots()
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
    if (clock.turn < clock.maxTurn) {
      clock.advanceTurn()
      return false
    } else {
      return true
    }
  }

  /**
   * Concludes the day, ingesting points and updating state.
   * Returns true if the game is over, false otherwise.
   */
  fun advanceDay(): Boolean {
    // Ingest points and award the best day on mountain.
    players.maxBy { it.day.mountainPoints }.day.bestDayPoints += BEST_DAY_AWARD[clock.day]!!
    players.forEach { it.ingestDayAndCopyNextDay() }
    playerOrder.sortBy { players[it].points }

    // Update clock, advance to next day if there is one.
    if (clock.day >= Clock.Params.MAX_DAY) {
      return false
    }

    clock.advanceDay()
    populateApresSlots()
    for (player in playersInTurnOrder()) {
      player.location = player.handler.getStartingLocation(player, this)
    }
    return true
  }

  /** Populates the apres slots for the current day. */
  private fun populateApresSlots() {
    apres.clear()
    for (i in 1..APRES_SLOTS) {
      apres.add(apresDeck.drawForDay(clock.day))
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
        check(subTurn == 0) { "Can only rest at start of turn." }
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

      MountainDecision.DecisionCase.DECISION_NOT_SET, null -> throw IllegalArgumentException()
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

    // Check slow condition.
    check(!(destinationTile.slope.slow && player.turn.speed > MAX_SPEED_ON_SLOW)) {
      "Cannot travel to SLOW tile with speed ${player.turn.speed}"
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
      player.day.overkillBonusPoints
        ?.takeIf { skill - difficulty >= it.threshold }
        ?.let { player.turn.points += it.bonus }
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
    val link = tile.apresLink
    check(link > 0) { "Location $location does not have an exit" }

    player.location = null
    player.apresLink = link
    apres[link - 1].apply(player, players.count { it.apresLink == link } == 1, this)
  }

  companion object {
    /** Maximum speed a player can have and still ski/ride a slow tile. */
    const val MAX_SPEED_ON_SLOW = 1

    /** Multiplier of speed to difficulty. */
    const val SPEED_DIFFICULTY_MODIFIER = 2

    /** Number of apres slots. */
    const val APRES_SLOTS = 3

    /** Value of the best day by day. */
    val BEST_DAY_AWARD = mapOf(1 to 15, 2 to 10, 3 to 5)
  }
}

/** Recording of time in game. */
interface Clock {
  /** What turn of day it is. */
  val turn: Int

  /** The max turn of this day. */
  val maxTurn: Int

  /** What game of day it is. */
  val day: Int

  object Params {
    const val MAX_DAY = 3
  }
}

class MutableClock(override var turn: Int = 1, override var day: Int = 1) : Clock {
  /** Returns the max turn of the day. */
  override val maxTurn: Int
    get() = when (day) {
      1 -> 9
      2 -> 8
      3 -> 7
      else -> 0
    }

  /** Advances to the next turn. */
  fun advanceTurn() {
    turn++
  }

  /** Advances to the next day. */
  fun advanceDay() {
    turn = 1
    day++
  }
}
