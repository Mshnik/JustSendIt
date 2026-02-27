package com.redpup.justsendit.model

import com.google.inject.Inject
import com.google.protobuf.util.Timestamps
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.*
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.TileMapBuilder
import com.redpup.justsendit.model.board.tile.proto.LiftColor
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecision.SkiRideDecision
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TimeSource

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

  /** The current player whose turn it is. */
  val currentPlayer: Player
}

/** Top level joined game model state. */
class MutableGameModel @Inject constructor(
  tileMapBuilder: TileMapBuilder,
  playerControllers: @JvmSuppressWildcards List<PlayerController>,
  playerDeck: PlayerDeck,
  playerFactory: PlayerFactory,
  private val apresDeck: ApresDeck,
  override val skillDecks: SkillDecks,
  private val timeSource: TimeSource,
  private val loggers: Set<Logger>,
) : GameModel {
  /** Applies fn to this. */
  override fun mutate(fn: MutableGameModel.() -> Unit) {
    this.fn()
  }

  override val tileMap: HexGrid<MountainTile> = tileMapBuilder.build()
  private val lifts =
    tileMap.entries().filter { it.value.hasLift() }
      .groupBy { it.value.lift.color }

  override val apres: MutableList<Apres> = mutableListOf()

  override val players: List<MutablePlayer> =
    playerControllers
      .map { handler -> playerFactory.create(playerDeck.draw(), handler) }

  private var currentPlayerIndex = 0
  private val playerOrder = MutableList(players.size) { it }
  override var currentPlayer = players[playerOrder[currentPlayerIndex]]

  override val clock = MutableClock()

  init {
    for (player in players) {
      player.buyStartingDeck(skillDecks)
      player.location = createHexPoint(0, 0)
    }
    populateApresSlots()
  }

  /** Adds this message as a log to this game model. */
  private fun Any.log() {
    val value = this
    log {
      timestamp = Timestamps.fromMillis(timeSource.now().toEpochMilli())
      day = clock.day
      turn = clock.turn
      subturn = clock.subTurn
      playerName = currentPlayer.name
      controllerName = currentPlayer.handler.name
      when (value) {
        is MountainDecision -> mountainDecision = value
        is PlayerMove -> playerMove = value
        is SkiRideAttempt -> skiRideAttempt = value
        else -> throw IllegalArgumentException("Unsupported log $value")
      }
    }.let { log -> loggers.forEach { it.log(log) } }
  }

  /**
   * Returns the places the given player could move to from their current
   * location when they ski/ride.
   */
  fun getAvailableMoves(player: Player): Map<HexPoint, HexDirection> {
    val location = player.location ?: return emptyMap()
    return HexDirection.entries
      .filter { it != HexDirection.HEX_DIRECTION_UNSET && it != HexDirection.UNRECOGNIZED }
      .filter { it.isDownMountain }
      .associateBy({ location + it }, { it })
      .filter { tileMap.contains(it.key) }
  }

  /** Returns the players in turn order. */
  private fun playersInTurnOrder() = playerOrder.map { players[it] }

  /** Executes one turn for the current player. */
  suspend fun turn() {
    if (currentPlayer.isOnMountain) {
      do {
        val decision = currentPlayer.handler
          .makeMountainDecision(currentPlayer, this)
          .also { it.log() }
        val continueTurn = executeDecision(currentPlayer, decision)
        clock.advanceSubTurn()
      } while (continueTurn)
      currentPlayer.abilityHandler.onAfterTurn(this)
      currentPlayer.ingestTurn()
    }

    currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    if (currentPlayerIndex == 0) { // Wrapped around
      if (clock.turn < clock.maxTurn) {
        clock.advanceTurn()
      } else {
        advanceDay()
      }
    }
    // Reset current player.
    currentPlayer = players[playerOrder[currentPlayerIndex]]
  }

  /**
   * Concludes the day, ingesting points and updating state.
   * Returns true if the game is over, false otherwise.
   */
  suspend fun advanceDay(): Boolean {
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
  private suspend fun executeDecision(
    player: MutablePlayer,
    decision: MountainDecision,
  ): Boolean {
    return when (decision.decisionCase) {
      MountainDecision.DecisionCase.SKI_RIDE -> executeSkiRide(player, decision.skiRide)
      MountainDecision.DecisionCase.REST -> {
        check(clock.isFirstSubTurn) { "Can only rest at start of turn." }
        executeRest(player)
        false
      }

      MountainDecision.DecisionCase.LIFT -> {
        executeLift(player)
        false
      }

      MountainDecision.DecisionCase.PASS -> {
        check(!clock.isFirstSubTurn) { "Can't pass without taking at least one action" }
        false
      }

      MountainDecision.DecisionCase.EXIT -> {
        executeExit(player)
        false
      }

      MountainDecision.DecisionCase.DECISION_NOT_SET, null -> throw IllegalArgumentException()
    }
  }

  private suspend fun executeSkiRide(
    player: MutablePlayer,
    skiRideDecision: SkiRideDecision,
  ): Boolean {
    // Check directions of ski/ride, make sure we don't go off mountain.
    val location = player.location
    check(location != null) { "Player is off-map." }
    check(skiRideDecision.direction.isDownMountain) { "Can only ski/ride down mountain, found ${skiRideDecision.direction}" }
    val destination = location + skiRideDecision.direction
    playerMove {
      from = location
      to = destination
    }.log()
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

    // Player moves to tile.
    player.location = destination

    // Actually play cards and compare to difficulty.
    val cards = (1..skiRideDecision.numCards).map { player.playSkillCard()!! }
    val bonus = player.computeBonus(destinationTile.slope)
    val baseDifficulty = destinationTile.slope.difficulty
    val speedDifficulty = player.turn.speed * SPEED_DIFFICULTY_MODIFIER
    val skill = cards.sum() + bonus
    val difficulty = baseDifficulty + speedDifficulty
    val halfDifficulty = difficulty.toDouble() / 2.0


    skiRideAttempt {
      this.baseDifficulty = baseDifficulty
      this.speedDifficulty = speedDifficulty
      cardValue += cards
      this.bonusValue = bonus
      success = skill >= difficulty
    }.log()

    // Compute and apply result to turn.
    val success = skill >= difficulty
    if (success) {
      player.turn.points += difficulty
      player.day.overkillBonusPoints
        ?.takeIf { skill - difficulty >= it.threshold }
        ?.let { player.turn.points += it.bonus }
      player.turn.speed++
      player.abilityHandler.onSuccessfulRun(this, skill - difficulty)
    }
    if (skill <= difficulty && skill >= halfDifficulty) {
      player.turn.experience++
    }

    // Turn continues if successful or if ability allows.
    return success || player.abilityHandler.onCrash(
      this,
      skill - difficulty,
      skill < halfDifficulty
    )
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
    val destination = getOtherLiftLocation(tile.lift.color, location)
    playerMove {
      from = location
      to = destination
    }.log()
    player.location = destination
    player.refreshSkillDeck()
  }

  /** Returns the matching lift location for [color] that is not [location]. */
  private fun getOtherLiftLocation(color: LiftColor, location: HexPoint): HexPoint =
    lifts[color]!!.find { it.key != location }!!.key

  /** Executes the player leaving the mountain. */
  private suspend fun executeExit(player: MutablePlayer) {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val tile = tileMap[location]!!
    val link = tile.apresLink
    check(link > 0) { "Location $location does not have an exit" }
    playerMove {
      from = location
    }.log()
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
