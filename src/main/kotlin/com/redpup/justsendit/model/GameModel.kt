package com.redpup.justsendit.model

import com.google.inject.Inject
import com.google.protobuf.util.Timestamps
import com.redpup.justsendit.control.player.PlayerController
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
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.LiftColor
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.*
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecision.SkiRideDecision
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.*
import com.redpup.justsendit.util.TimeSource

/** Immutable access to game model. */
interface GameModel {
  /** Mutates this game model with the given fn. */
  fun mutate(fn: MutableGameModel.() -> Unit)

  /** The mountain map. */
  val tileMap: HexGrid<MountainTile>

  /** Points per mountain tile. */
  val tileMapPoints: HexGrid<Int>

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
  @StarterDeck private val starterDeck: SkillDeck,
  @ShopDeck override val skillDeck: SkillDeck,
  private val timeSource: TimeSource,
  private val loggers: Set<Logger>,
) : GameModel {
  /** Applies fn to this. */
  override fun mutate(fn: MutableGameModel.() -> Unit) {
    this.fn()
  }

  override val tileMap: HexGrid<MountainTile> = tileMapBuilder.build()
  override val tileMapPoints: HexGrid<Int> = tileMap.map { _, _ -> 0 }
  private val lifts =
    tileMap.entries().filter { it.value.hasLift() }.groupBy { it.value.lift.color }

  override val apres: MutableList<Apres> = mutableListOf()

  override val players: List<MutablePlayer> = playerControllers.map { MutablePlayer(it) }

  private var currentPlayerIndex = 0
  private val playerOrder = MutableList(players.size) { it }
  override val currentPlayer get() = players[playerOrder[currentPlayerIndex]]

  private val passedPlayers = mutableSetOf<Int>()

  private val shop = mutableListOf<Skill>()
  private val saleTokens = mutableMapOf<Skill, Int>()

  override val clock = MutableClock()

  private val random = java.util.Random()

  private fun matches(
    icon: com.redpup.justsendit.model.proto.Icon,
    slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
  ): Boolean {
    return when (icon.typeCase) {
      com.redpup.justsendit.model.proto.Icon.TypeCase.GRADE -> icon.grade == slope.grade
      com.redpup.justsendit.model.proto.Icon.TypeCase.CONDITION -> icon.condition == slope.condition
      com.redpup.justsendit.model.proto.Icon.TypeCase.HAZARD -> slope.hazardsList.contains(icon.hazard)
      com.redpup.justsendit.model.proto.Icon.TypeCase.WILD -> icon.wild
      else -> false
    }
  }

  private data class CardResolution(val skill: Int, val wobbles: Int)

  private fun resolveCard(
    player: MutablePlayer,
    skill: Skill,
    slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
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

    // Step 2: Roll dice
    val rolls = mutableListOf<Int>()
    repeat(green) { rolls.add(DieType.GREEN.roll(random)) }
    repeat(blue) { rolls.add(DieType.BLUE.roll(random)) }
    repeat(black) { rolls.add(DieType.BLACK.roll(random)) }

    // Step 3: All other terrain and effects (including rerolls) - TODO

    // Step 4: Check for and gain wobbles
    var wobbles = rolls.count { it == 1 }
    // Ice: [After roll] Gain an additional wobble for each 1 rolled.
    if (slope.condition == Condition.CONDITION_ICY) {
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
    val matchingIcons = skill.skillCard.iconsList.count { matches(it, slope) }
    sum += matchingIcons

    return CardResolution(sum, wobbles)
  }

  private fun crash(player: MutablePlayer) {
    player.resetWobbles()
    if (player.hand.isNotEmpty()) {
      // Discard one card from hand if they have any remaining.
      player.discardFromHand(player.hand.first())
    } else {
      // Wipeout! Lose 10 fun.
      player.setPoints((player.points - 10).coerceAtLeast(0))
    }
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
      controllerName = currentPlayer.controller.name
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

  /** Randomly determines a leader and sets the starting player order. */
  private fun setStartingPlayerOrder() {
    playerOrder.shuffle()
    for ((index, playerIdx) in playerOrder.withIndex()) {
      players[playerIdx].setPoints(10 + (index * 2))
    }
  }

  /** Gives each player a pick of the player cards, in order player. */
  private suspend fun pickPlayerCards() {
    val cards = playerDeck.draw(clock.day, players.size + 2)
    for (player in players) {
      val card = player.controller.choosePlayerCard(player, cards)
      player.gainPlayerCard(card)
    }
  }

  /** Adds apres cards to each of the apres slots. */
  private fun populateApresSlots() {
    apres.clear()
    for (i in 1..APRES_SLOTS) {
      apres.add(apresDeck.drawForDay(clock.day))
    }
  }

  /** Adds fun to mountain tiles. */
  private fun populateMountainPoints() {
    for (point in tileMap.keys()) {
      if (tileMap[point]!!.hasSlope()) {
        tileMapPoints[point] = tileMapPoints[point]!! + MOUNTAIN_POINTS
      }
    }
  }

  /** Starts a new day. May be the first day of the game or a later day in the game. */
  suspend fun startDay() {
    pickPlayerCards()
    populateApresSlots()
    populateMountainPoints()
    replenishShop()

    if (clock.day == Day.DAY_FRIDAY) {
      setStartingPlayerOrder()
      giveStarterDecks()
    }

    for (player in playersInTurnOrder()) {
      player.location = player.controller.getStartingLocation(player, this)
      player.playerCards.forEach { it.startDay() }
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

  /** Replenishes the shop to 5 cards, adding sale tokens to existing cards and removing old ones. */
  private fun replenishShop() {
    // Add sale tokens to existing cards
    for (card in shop) {
      saleTokens[card] = (saleTokens[card] ?: 0) + 1
    }

    // Remove cards with 2 sale tokens
    val toRemove = shop.filter { (saleTokens[it] ?: 0) >= 2 }
    shop.removeAll(toRemove)
    toRemove.forEach { saleTokens.remove(it) }

    // Replenish up to 5 cards
    while (shop.size < 5) {
      shop.add(skillDeck.draw())
    }
  }

  /** Executes one turn for the current player. */
  suspend fun turn() {
    if (!passedPlayers.contains(playerOrder[currentPlayerIndex])) {
      val player = currentPlayer
      player.playerCards.forEach { it.startTurn() }

      var continueTurn: Boolean
      do {
        val decision =
          player.controller.makeMountainDecision(player, this).also { it.log() }
        continueTurn = executeDecision(player, decision)
      } while (continueTurn)
    }

    // Move to next player who hasn't passed
    do {
      currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    } while (passedPlayers.size < players.size && passedPlayers.contains(playerOrder[currentPlayerIndex]))

    if (passedPlayers.size == players.size) { // Round over
      endRound()
    }
  }

  /** Concludes the round, resets passed players, and moves leader token. */
  private suspend fun endRound() {
    passedPlayers.clear()
    // Rulebook V2: "At the end of the round ... add a 'sale' token ... remove all cards that have two sale tokens and replenish the shop up to five cards."
    replenishShop()

    // Move leader: the playerOrder currently represents the turn order.
    // In Rulebook V2: "At the end of the round, the leader token moves clockwise for the next round."
    val first = playerOrder.removeFirst()
    playerOrder.add(first)
    currentPlayerIndex = 0

    if (clock.turn < clock.maxTurn) {
      clock.advanceTurn()
    } else {
      advanceDay()
    }
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
    return when (decision.decisionCase) {
      MountainDecision.DecisionCase.SKI_RIDE -> {
        executeSkiRide(player, decision.skiRide)
      }

      MountainDecision.DecisionCase.LIFT -> {
        executeLift(player, decision.lift)
      }

      MountainDecision.DecisionCase.PASS -> {
        executePass(player, decision.pass)
        passedPlayers.add(playerOrder[currentPlayerIndex])
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
      val action = player.controller.chooseSkiRideResolutionAction(player, this)
      if (action.hasStop()) {
        crash(player)
        break
      }

      val card = player.hand.find { it.name == action.playCardName }
      if (card == null) {
        crash(player)
        break
      }

      player.playCard(card)

      val resolution = resolveCard(player, card, slope)
      cumulativeSkill += resolution.skill
      player.addWobbles(resolution.wobbles)

      if (player.wobbles >= 3) {
        crash(player)
        break
      }

      if (cumulativeSkill >= slope.difficulty) {
        succeeded = true
        player.mutate { points += slope.difficulty }
      } else if (player.hand.isEmpty()) {
        crash(player)
        break
      }
    }

    skiRideAttempt {
      this.baseDifficulty = slope.difficulty
      this.success = succeeded
    }.log()

    return succeeded
  }

  private fun executeRest(player: MutablePlayer) {
    player.refreshDecks()
    PlayerGameEvent.PlayerRested.broadcast(player)
  }

  private fun getLiftCost(color: LiftColor): Int {
    return when (color) {
      LiftColor.LIFT_COLOR_RED -> 1
      LiftColor.LIFT_COLOR_YELLOW -> 2
      LiftColor.LIFT_COLOR_CYAN -> 3
      LiftColor.LIFT_COLOR_MAGENTA -> 4
      LiftColor.LIFT_COLOR_GREY -> 5
      else -> 1
    }
  }

  private suspend fun executeLift(
    player: MutablePlayer,
    liftDecision: MountainDecision.LiftDecision,
  ): Boolean {
    val location = player.location
    check(location != null) { "Player is off-map." }
    val tile = tileMap[location]!!
    check(tile.hasLift()) { "Location $location does not have a lift" }

    if (liftDecision.actionCase == MountainDecision.LiftDecision.ActionCase.STAY) {
      return false
    }

    val cost = getLiftCost(tile.lift.color)
    val toDiscard = player.controller.chooseCardsToDiscardForLift(player, player.hand, cost)
    check(toDiscard.size == cost) { "Must discard $cost cards, got ${toDiscard.size}" }

    toDiscard.forEach { player.discardFromHand(it) }

    // Rulebook: "choose to trash up the same number of cards from their discard pile ... optionally including the card(s) just discarded."
    val trashCandidates = player.skillDiscard.toList()
    val toTrash = player.controller.chooseCardsToTrashForLift(player, trashCandidates, cost)
    toTrash.forEach {
      player.skillDiscard.remove(it)
    }

    ApresGameEvent.PlayerUsedLift.broadcast()
    val destination = getOtherLiftLocation(tile.lift.color, location)
    playerMove {
      from = location
      to = destination
    }.log()
    player.location = destination

    return false
  }

  private suspend fun executeActivatePlayerCard(player: MutablePlayer, playerCardName: String) {
    val playerCard = player.playerCards.find { it.name == playerCardName }
      ?: throw IllegalArgumentException("No player card $playerCardName in ${player.playerCards}.")
    playerCard.activate(player, this)
  }

  private fun getOtherLiftLocation(color: LiftColor, location: HexPoint): HexPoint =
    lifts[color]!!.find { it.key != location }!!.key

  private fun executePass(player: MutablePlayer, passDecision: MountainDecision.PassDecision) {
    player.discardInPlay()

    val studyValue = calculateStudyValue(player)
    val buyCardName = passDecision.buyCardName
    if (buyCardName.isNotEmpty()) {
      val card = shop.find { it.name == buyCardName }
      check(card != null) { "Card $buyCardName not in shop." }
      val cost = (card.skillCard.cost - (saleTokens[card] ?: 0)).coerceAtLeast(0)
      check(studyValue >= cost) { "Insufficient study value $studyValue for card $buyCardName (cost $cost)." }

      shop.remove(card)
      saleTokens.remove(card)
      player.skillDeck.add(card) // Rulebook doesn't specify where it goes, assuming deck for now.
    }
  }

  private fun calculateStudyValue(player: Player): Int {
    var total = player.hand.size
    val currentLocation = player.location ?: return total
    val tile = tileMap[currentLocation] ?: return total

    if (tile.hasSlope()) {
      val slope = tile.slope
      for (skill in player.hand) {
        total += skill.skillCard.iconsList.count { matches(it, slope) }
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
    apres[link - 1].apply(player, players.count { it.apresLink == link } == 1, this)
  }

  companion object {
    const val APRES_SLOTS = 3
    const val MOUNTAIN_POINTS = 5
  }
}
