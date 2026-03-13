package com.redpup.justsendit.model

import com.google.inject.Inject
import com.google.protobuf.util.Timestamps
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.*
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.TileMapBuilder
import com.redpup.justsendit.model.board.tile.proto.LiftColor
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.*
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecision.SkiRideDecision
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TimeSource
import com.redpup.justsendit.util.peek

/** Immutable access to game model. */
interface GameModel {
  /** Mutates this game model with the given fn. */
  fun mutate(fn: MutableGameModel.() -> Unit)

  /** The mountain map. */
  val tileMap: HexGrid<MountainTile>

  /** Immutable access to all players. */
  val players: List<Player>

  /**
   * Returns the places the given player could move to from their current
   * location when they ski/ride.
   */
  fun getAvailableMoves(player: Player): Map<HexPoint, HexDirection>

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
  playerFactory: PlayerFactory,
  private val playerDeck: PlayerDeck,
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
    tileMap.entries().filter { it.value.hasLift() }.groupBy { it.value.lift.color }

  override val apres: MutableList<Apres> = mutableListOf()

  override val players: List<MutablePlayer> = playerControllers.map { playerFactory.create(it) }

  private var currentPlayerIndex = 0
  private val playerOrder = MutableList(players.size) { it }
  override var currentPlayer = players[playerOrder[currentPlayerIndex]]

  override val clock = MutableClock()

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

  override fun getAvailableMoves(player: Player): Map<HexPoint, HexDirection> {
    val location = player.location ?: return emptyMap()
    return HexDirection.entries.filter { it != HexDirection.HEX_DIRECTION_UNSET && it != HexDirection.UNRECOGNIZED }
      .filter { it.isDownMountain }.associateBy({ location + it }, { it })
      .filter { tileMap.contains(it.key) }
  }

  /** Returns the players in turn order. */
  private fun playersInTurnOrder() = playerOrder.map { players[it] }

  /** Has each player play the top card of their skill deck, then orders from highest to lowest. */
  private fun setStartingPlayerOrder() {
    val topCards = players.map { it.playSkillCard()!! }
    playerOrder.sortByDescending { topCards[it] }
  }

  /** Gives each player a pick of the player cards, in order player. */
  private suspend fun pickPlayerCards() {
    val cards = playerDeck.draw(clock.day, players.size + 2)
    for (player in players) {
      val card = player.handler.choosePlayerCard(player, cards)
      player.gainPlayerCard(card, skillDecks)
    }
  }

  /** Starts a new day. May be the first day of the game or a later day in the game. */
  suspend fun startDay() {
    pickPlayerCards()
    populateApresSlots()

    if (clock.day == Day.DAY_FRIDAY) {
      setStartingPlayerOrder()
    }

    for (player in playersInTurnOrder()) {
      player.location = player.handler.getStartingLocation(player, this)
    }
  }

  /** Executes one turn for the current player. */
  suspend fun turn() {
    if (currentPlayer.isOnMountain) {
      do {
        val decision =
          currentPlayer.handler.makeMountainDecision(currentPlayer, this).also { it.log() }
        val continueTurn = executeDecision(currentPlayer, decision)
        clock.advanceSubTurn()
      } while (continueTurn)
      currentPlayer.ingestTurn()
      clock.resetSubTurn()
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
    players.forEach { it.ingestDayAndCopyNextDay() }
    playerOrder.sortBy { players[it].points }

    // Update clock, advance to next day if there is one.
    if (clock.day == Day.DAY_SUNDAY) {
      return false
    }

    clock.advanceDay()
    startDay()
    return true
  }

  private fun populateApresSlots() {
    apres.clear()
    for (i in 1..APRES_SLOTS) {
      apres.add(apresDeck.drawForDay(clock.day))
    }
  }

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

    if (destinationTile.hasLift()) {
      player.location = destination
      check(skiRideDecision.numCards == 0) { "Must play 0 cards to ski/ride onto lift, got ${skiRideDecision.numCards}" }
      return true
    }

    check(!(destinationTile.slope.slow && player.turn.speed > MAX_SPEED_ON_SLOW)) {
      "Cannot travel to SLOW tile with speed ${player.turn.speed}"
    }
    check(skiRideDecision.numCards >= 1 && skiRideDecision.numCards <= clock.maxCards) {
      "Must play between [1,${clock.day}] cards, got ${skiRideDecision.numCards}"
    }
    check(skiRideDecision.numCards <= player.skillDeck.size) {
      "Only ${player.skillDeck.size} cards remaining, cannot play ${skiRideDecision.numCards} cards"
    }

    player.location = destination

    val cards = (1..skiRideDecision.numCards).map { player.playSkillCard()!! }
    val baseDifficulty = destinationTile.slope.difficulty
    val speedDifficulty = player.turn.speed * SPEED_DIFFICULTY_MODIFIER
    var skill = cards.sum()
    val difficulty = baseDifficulty + speedDifficulty

    var success = skill >= difficulty
    var bonus = 0
    if (!success) {
      bonus = player.handler.chooseChipsToUse(player, destinationTile.slope, skill, difficulty)
        .peek { player.playTrainingChip(it) }
        .sumOf { if (it.appliesTo(destinationTile.slope)) it.value() else 1 }

      skill += bonus
      success = skill >= difficulty
    }

    skiRideAttempt {
      this.baseDifficulty = baseDifficulty
      this.speedDifficulty = speedDifficulty
      cardValue += cards
      this.bonusValue = bonus
      this.success = success
    }.log()

    if (success) {
      player.turn.points += difficulty
      player.turn.speed++
    } else {
      player.turn.points = 0
    }

    return success
  }

  private fun executeRest(player: MutablePlayer) {
    player.refreshDecksAndChips()
  }

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
    player.refreshDecksAndChips()
  }

  private fun getOtherLiftLocation(color: LiftColor, location: HexPoint): HexPoint =
    lifts[color]!!.find { it.key != location }!!.key

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
    const val MAX_SPEED_ON_SLOW = 1
    const val SPEED_DIFFICULTY_MODIFIER = 2
    const val APRES_SLOTS = 3
  }
}
