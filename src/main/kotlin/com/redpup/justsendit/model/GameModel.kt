package com.redpup.justsendit.model

import com.google.common.collect.Range
import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.protobuf.util.Timestamps
import com.redpup.justsendit.control.*
import com.redpup.justsendit.control.PlayerController.*
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.*
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresGameEvent
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.TileMapBuilder
import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.clock.Clock
import com.redpup.justsendit.model.clock.MaxRound
import com.redpup.justsendit.model.player.Icons.matches
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.GameState
import com.redpup.justsendit.model.random.Dice.roll
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.*
import com.redpup.justsendit.util.TimeSource

/** Immutable access to game model. */
interface GameModel {
  /** Mutates this game model with the given fn. */
  fun mutate(fn: MutableGameModel.() -> Unit)

  /** The current state of the game. */
  val state: GameState get() = clock.state

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
  val skillDeck: SkillDeck

  /** The shop where players can buy skills. Map of skill to sale tokens. */
  val shop: Map<Skill, Int>

  /** The current player whose turn it is. */
  val currentPlayer: Player
}

/** Top level joined game model state. */
@Singleton
class MutableGameModel @Inject constructor(
  tileMapBuilder: TileMapBuilder,
  playerControllers: @JvmSuppressWildcards List<PlayerController>,
  playerFactory: PlayerFactory,
  override val clock: Clock,
  @MaxRound private val maxRound: Int,
  private val playerDeck: PlayerDeck,
  private val apresDeck: ApresDeck,
  @StarterDeck private val starterDeck: SkillDeck,
  @ShopDeck override val skillDeck: SkillDeck,
  private val timeSource: TimeSource,
  private val random: Random,
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

  override val players: MutableList<MutablePlayer> =
    playerControllers.map { MutablePlayer(it) }.toMutableList()

  private var currentPlayerIndex = 0
  override val currentPlayer get() = players[currentPlayerIndex]

  override val shop = mutableMapOf<Skill, Int>()

  private data class CardResolution(
    val rolls: List<Int>,
    val skill: Int,
    val iconBonus: Int,
    val wobbles: Int,
  )

  private fun resolveCard(
    player: MutablePlayer,
    skill: Skill,
    slope: SlopeTile,
  ): CardResolution {
    var green = skill.skillCard.greenDice
    var blue = skill.skillCard.blueDice
    var black = skill.skillCard.blackDice

    // Step 1: Terrain and effects that change dice
    // Powder: [Before roll] First card only: Remove your lowest die.
    if (slope.condition == Condition.CONDITION_POWDER && player.inPlay.size == 1) {
      if (green > 0) green--
      else if (blue > 0) blue--
      else if (black > 0) black--
    }

    // Moguls: [Before roll] Downgrade your highest die.
    if (slope.hazardsList.contains(Hazard.HAZARD_MOGULS)) {
      if (black > 0) {
        black--
        blue++
      } else if (blue > 0) {
        blue--
        green++
      }
    }

    val dice = buildList {
      repeat(green) { add(Die.DIE_GREEN) }
      repeat(blue) { add(Die.DIE_BLUE) }
      repeat(black) { add(Die.DIE_BLACK) }
    }

    // Step 2: Roll dice
    val rolls = dice.map { it.roll(random) }

    // Step 3: All other terrain and effects (including rerolls) - TODO

    // Step 4: Check for and gain wobbles
    var wobbles = rolls.count { it == 1 }
    // Ice: [After roll] Gain an additional wobble for each 1 rolled.
    if (slope.condition == Condition.CONDITION_ICE) {
      wobbles += rolls.count { it == 1 }
    }

    // Step 6: Sum skill
    var sum = rolls.sum()
    // Trees: [After roll] All rolled 5s score 0 skill.
    if (slope.hazardsList.contains(Hazard.HAZARD_TREES)) {
      sum -= rolls.count { it == 5 } * 5
    }
    // Cliffs: [After roll] All rolled 2s and 3s score 0 skill.
    if (slope.hazardsList.contains(Hazard.HAZARD_CLIFFS)) {
      sum -= rolls.count { it == 2 } * 2
      sum -= rolls.count { it == 3 } * 3
    }

    // Matching icons (+1 each)
    val matchingIcons = skill.skillCard.iconsList.count { it.matches(slope) }
    sum += matchingIcons

    return CardResolution(rolls, sum, matchingIcons, wobbles)
  }

  /** Adds this message as a log to this game model. */
  private fun Any.log() {
    if (loggers.isEmpty()) {
      return
    }

    val value = this
    log {
      timestamp = Timestamps.fromMillis(timeSource.now().toEpochMilli())
      day = clock.day
      round = clock.round
      turn = clock.turn
      subturn = clock.subTurn
      playerName = currentPlayer.name
      controllerName = currentPlayer.controller.name
      when (value) {
        is GameState -> stateTransition = value
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

  /** Gives each player a pick of the player cards, in order player. */
  private suspend fun pickPlayerCards() {
    val cards = playerDeck.draw(clock.day, players.size + 2)
    for (player in players) {
      with(player) {
        gainPlayerCard(
          controller.choosePlayerCard(
            this@MutableGameModel,
            player,
            cards
          )
        )
      }
    }
  }

  /** Adds apres cards to each of the apres slots. */
  private fun populateApresSlots() {
    apres.clear()
    for (i in 1..APRES_SLOTS) {
      apres.add(apresDeck.drawForDay(clock.day))
    }
  }

  /** Starts the game. */
  suspend fun startGame() {
    clock.startGame()
    giveStartingPoints()
    giveStarterDecks()
    startDay()
  }

  /** Randomly determines a leader and sets the starting player order. */
  private fun giveStartingPoints() {
    for ((index, player) in players.withIndex()) {
      player.points += 10 + (index * 2)
    }
  }

  /** Gives each player their 10-card starter deck. */
  private fun giveStarterDecks() {
    for (player in players) {
      starterDeck.reset()
      repeat(10) {
        player.gainSkill(starterDeck.draw())
      }
    }
  }

  /** Starts a new day. May be the first day of the game or a later day in the game. */
  private suspend fun startDay() {
    clock.startDay()

    pickPlayerCards()
    populateApresSlots()
    replenishShop()

    for (player in players) {
      player.location = player.controller.chooseMountainTile(
        this,
        player,
        ChooseStartOfDayLocation,
        tileMap.entries()
          .filter { it.value.hasLift() && it.value.lift.direction == LiftDirection.LIFT_DIRECTION_TOP }
          .map { it.key }
      )
      player.playerCards.forEach { it.startDay() }
    }

    startRound()
  }

  /**
   * Replenishes the shop to 5 cards, adding sale tokens to existing cards and removing old
   * ones.
   */
  private fun replenishShop() {
    // Add sale tokens to existing cards.
    shop.mapValues { it.value + 1 }

    // Remove cards with 2 sale tokens.
    shop.filterValues { it <= 1 }

    // Replenish up to SHOP_SIZE cards.
    while (shop.size < SHOP_SIZE) {
      shop[skillDeck.draw()] = 0
    }
  }

  /** Starts a round. */
  private fun startRound() {
    clock.startRound()

    players.forEach {
      it.discardInPlay()
      it.discardHand()
      it.drawCards(INITIAL_HAND_SIZE, random)
    }
  }

  /** Executes one turn for the current player. */
  suspend fun turn() {
    clock.startTurn()
    check(!currentPlayer.isPassed) { "Player $currentPlayer has passed" }

    val player = currentPlayer
    player.playerCards.forEach { it.startTurn() }

    var continueTurn: Boolean
    do {
      val decision = player.controller.makeMountainDecision(this, player).also { it.log() }
      continueTurn = executeDecision(player, decision)
      clock.incrementSubTurn()
    } while (continueTurn)

    clock.endTurn()

    // If not all players have passed, advance to the next non-passed player.
    if (players.all { it.isPassed }) {
      clock.endRound()
      if (clock.round >= maxRound) {
        clock.endDay()
      }
    } else {
      do {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
      } while (currentPlayer.isPassed)
    }
  }

  /** Broadcasts this [ApresGameEvent] to every [Apres] card. */
  private fun ApresGameEvent.broadcast() {
    apres.forEach { it.handleGameEvent(this@broadcast, this@MutableGameModel) }
  }

  /** Broadcasts this [PlayerGameEvent] to every [PlayerCard] in [player]. */
  private fun PlayerGameEvent.broadcast(player: MutablePlayer) {
    player.playerCards.forEach { it.handleGameEvent(this@broadcast, player, this@MutableGameModel) }
  }

  /**
   * Executes the given [decision] for [player]. Returns true iff the player's turn continues
   * or false if it is now over.
   */
  private suspend fun executeDecision(
    player: MutablePlayer,
    decision: MountainDecision,
  ): Boolean {
    return when (decision) {
      MountainDecision.DECISION_SKI_RIDE -> {
        executeSkiRide(player)
      }

      MountainDecision.DECISION_LIFT -> {
        executeLift(player)
        false
      }

      MountainDecision.DECISION_PASS -> {
        executePass(player)
        false
      }

      MountainDecision.DECISION_EXIT -> {
        executeExit(player)
        false
      }

      MountainDecision.DECISION_UNSET, MountainDecision.UNRECOGNIZED -> throw IllegalArgumentException()
    }
  }

  private suspend fun executeSkiRide(
    player: MutablePlayer,
  ): Boolean {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val choices = getAvailableMoves(player)
    val direction = player.controller.chooseMountainTile(
      this, player,
      ChooseSkiRideDestination,
      choices.keys
    )
      .let { choices[it] ?: throw IllegalStateException("Illegal point chosen: $it") }

    check(direction.isDownMountain) { "Can only ski/ride down mountain, found ${direction}" }
    val destination = location + direction
    playerMove {
      from = location
      to = destination
    }.log()
    val destinationTile = tileMap[destination]
    check(destinationTile != null) { "Destination is invalid: $destination" }

    if (destinationTile.hasLift()) {
      player.location = destination
      return true
    }

    val slope = destinationTile.slope
    // Slow check: can only enter if 2 or fewer skill cards in play.
    if (slope.slow) {
      check(player.inPlay.size <= 2) { "Cannot enter slow tile with ${player.inPlay.size} cards in play" }
    }

    player.location = destination

    var cumulativeSkill = 0
    var succeeded = false

    while (!succeeded) {
      val cards = player.controller.chooseSkillCards(
        this,
        player,
        PlaySkillForSkiRideAttempt(slope, cumulativeSkill, player.wobbles),
        player.hand,
        Range.closed(0, 1),
        SkillZone.HAND
      )

      if (cards.isEmpty()) {
        crash(player, SkiRideCrash.Cause.CAUSE_INTENTIONAL)
        break
      }

      val card = cards.first()
      player.playCard(card)

      val resolution = resolveCard(player, card, slope)
      cumulativeSkill += resolution.skill
      player.addWobbles(resolution.wobbles)

      succeeded = cumulativeSkill >= slope.difficulty && player.wobbles < 3

      skiRideAttempt {
        this.totalTileDifficulty = slope.difficulty
        this.cardName = card.name
        this.rolledValues += resolution.rolls
        this.totalIconValue = resolution.iconBonus
        this.cumulativeSkill = cumulativeSkill
        this.cumulativeWobbles = player.wobbles
        this.success = succeeded
      }.log()

      if (player.wobbles >= 3) {
        crash(player, SkiRideCrash.Cause.CAUSE_WOBBLES)
        break
      }

      if (succeeded) {
        player.mutate { points += slope.difficulty }
      } else if (player.hand.isEmpty()) {
        crash(player, SkiRideCrash.Cause.CAUSE_NO_CARDS)
        break
      }
    }

    return succeeded
  }

  /** Called when [player] crashes. */
  private suspend fun crash(player: MutablePlayer, cause: SkiRideCrash.Cause) {
    if (player.hand.isNotEmpty()) {
      with(player) {
        controller.chooseSkillCards(
          this@MutableGameModel,
          player,
          DiscardForCrash,
          player.hand,
          Range.closed(1, 1),
          SkillZone.HAND
        ).forEach { discardFromHand(it) }
      }
    } else {
      player.points -= 10
    }
    player.resetWobbles()
    skiRideCrash { this.cause = cause }.log()
  }

  private suspend fun executeLift(
    player: MutablePlayer,
  ) {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val tile = tileMap[location]!!
    check(tile.hasLift()) { "Location $location does not have a lift" }

    // TODO: Don't discard for lift, play instead.
    val toPlay = player.controller.chooseSkillCards(
      this,
      player,
      PlaySkillForLift,
      player.hand,
      Range.closed(tile.lift.minCards, tile.lift.maxCards),
      SkillZone.HAND
    )

    toPlay.forEach { player.playCard(it) }

    // TODO: Add play, hand to trash candidates.
    val trashCandidates = buildList {
      addAll(player.skillDiscard)
      addAll(player.inPlay)
      addAll(player.hand)
    }
    player.controller.chooseSkillCards(
      this,
      player,
      TrashSkill,
      trashCandidates,
      Range.closed(0, toPlay.size),
      SkillZone.PLAY, SkillZone.DISCARD,
    ).forEach {
      player.skillDiscard.remove(it)
    }

    ApresGameEvent.PlayerUsedLift.broadcast()
    val destination = getOtherLiftLocation(tile.lift.color, location)
    playerMove {
      from = location
      to = destination
    }.log()
    player.location = destination
  }

  private suspend fun executeActivatePlayerCard(player: MutablePlayer, playerCardName: String) {
    val playerCard = player.playerCards.find { it.name == playerCardName }
      ?: throw IllegalArgumentException("No player card $playerCardName in ${player.playerCards}.")
    playerCard.activate(player, this)
  }

  private fun getOtherLiftLocation(color: LiftColor, location: HexPoint): HexPoint =
    lifts[color]!!.find { it.key != location }!!.key

  /** Ends the player's round and allows them to buy a card. */
  private suspend fun executePass(player: MutablePlayer) {
    val studyValue = calculateStudyValue(player)
    val cards = player.controller.chooseSkillCards(
      this,
      player,
      ChooseCardToBuy(studyValue),
      shop.keys.toList(),
      Range.closed(0, 1),
      SkillZone.SHOP
    )
    if (cards.isNotEmpty()) {
      val card = cards.first()
      val cost = (card.skillCard.cost - shop[card]!!).coerceAtLeast(0)
      check(studyValue >= cost) { "Insufficient study value $studyValue for card ${card.name} (cost $cost)." }

      shop.remove(card)
      player.skillDiscard.add(card)
    }

    player.discardInPlay()
    player.isPassed = true
  }

  private fun calculateStudyValue(player: Player): Int {
    var total = player.hand.size
    val currentLocation = player.location ?: return total
    val tile = tileMap[currentLocation] ?: return total

    if (tile.hasSlope()) {
      val slope = tile.slope
      for (skill in player.hand) {
        total += skill.skillCard.iconsList.count { it.matches(slope) }
      }
    } else if (tile.hasLift()) {
      for (skill in player.hand) {
        total += skill.skillCard.iconsList.count { it.hasWild() && it.wild }
      }
    }

    return total
  }

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
    apres[link - 1].apply(player, players.count { it.apresLink == link } == 1, this, random)
  }

  /** Concludes the round, resets passed players, and moves leader token. */
  suspend fun endRound() {
    replenishShop()

    // "Un-pass" each player.
    players.forEach { it.isPassed = false }
    // Move leader to back of line.
    val first = players.removeFirst()
    players.add(first)
    // Reset current player to new leader.
    currentPlayerIndex = 0

    clock.endRound()
  }

  /**
   * Concludes the day, ingesting points and updating state.
   * Returns true if the game is over, false otherwise.
   */
  suspend fun advanceDay(): Boolean {
    clock.endDay()

    // Update clock, advance to next day if there is one.
    if (clock.day == Day.DAY_SUNDAY) {
      return false
    }

    startDay()
    return true
  }

  companion object {
    const val APRES_SLOTS = 3
    const val MOUNTAIN_POINTS = 5

    const val INITIAL_HAND_SIZE = 5
    const val SHOP_SIZE = 5
  }
}
