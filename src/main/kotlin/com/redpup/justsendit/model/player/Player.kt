package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.player.proto.TrainingChip
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks

/** Immutable access to a player object. */
interface Player {
  /** Applies the given mutation function to this player. */
  fun mutate(fn: MutablePlayer.() -> Unit)

  /** The name of the player. */
  val name: String

  /** The player cards and upgrades the player has acquired. */
  val playerCards: List<PlayerCard>

  /** Hooks for this player's abilities. */
  val abilityHandler: AbilityHandler

  /** The handler for making decisions. */
  val handler: PlayerController

  /** How many points (fun) this player has. */
  val points: Int

  /** The player's current skill deck, in order. */
  val skillDeck: List<Int>

  /** The player's current skill discard, unordered. */
  val skillDiscard: Collection<Int>

  /** The terrain chips currently available to the player. */
  val trainingChips: List<TrainingChip>

  /** The terrain chips the player has used. */
  val usedTrainingChips: List<TrainingChip>

  /** The player's current location on the mountain. Null if not on mountain. */
  val location: HexPoint?

  /** Returns true iff the player is on the mountain. */
  val isOnMountain: Boolean get() = location != null

  /** The apres link the player took this day. Null if still on mountain today. */
  val apresLink: Int?

  /** A single day for the player. */
  interface Day {
    /** How many points (fun) this player has gained in this day on the mountain. */
    val mountainPoints: Int

    /** How many points (fun) this player has gained in this day by having the best day on the mountain. */
    val bestDayPoints: Int

    /** How many points (fun) this player has gained in this day off the mountain. */
    val apresPoints: Int
  }

  /** A single turn for the player. */
  interface Turn {
    /** How many points (fun) this player has gained in this turn. */
    val points: Int

    /** How much speed the player has in this turn. */
    val speed: Int
  }

  /** The player's current turn, if any. */
  val turn: Turn

  /** The player's current day, if any. */
  val day: Day
}

/** Hooks for player abilities. */
interface AbilityHandler {}

/** Mutable access to a player object. */
class MutablePlayer(override val handler: PlayerController) : Player {
  override val playerCards = mutableListOf<PlayerCard>()
  override val name: String get() = playerCards.firstOrNull()?.name ?: "No Name"
  override var points = 0; private set
  override var location: HexPoint? = null
  override var apresLink: Int? = null

  override val turn = MutableTurn()
  override val day = MutableDay()
  val nextDay = MutableDay()

  override val skillDeck = mutableListOf<Int>()
  override val skillDiscard = mutableListOf<Int>()
  override val trainingChips = mutableListOf<TrainingChip>()
  override val usedTrainingChips = mutableListOf<TrainingChip>()

  override val abilityHandler = object : AbilityHandler {}

  /** Applies [fn] to this player. */
  override fun mutate(fn: MutablePlayer.() -> Unit) {
    this.fn()
  }

  /** Plays the top card of the skill deck, returning it and putting it in the discard. */
  fun playSkillCard(): Int? {
    val card = skillDeck.removeFirstOrNull()
    if (card != null) {
      skillDiscard.add(card)
    }
    return card
  }

  /** Uses the given [chip]. */
  fun playTrainingChip(chip: TrainingChip) {
    check(trainingChips.remove(chip))
    usedTrainingChips.add(chip)
  }

  /** Shuffles all cards from discard back into skill deck. */
  fun refreshDecksAndChips() {
    skillDeck.addAll(skillDiscard)
    skillDeck.shuffle()
    skillDiscard.clear()
    trainingChips.addAll(usedTrainingChips)
    usedTrainingChips.clear()
  }

  /** Ingests the contents of [turn] into this player. */
  fun ingestTurn() {
    day.mountainPoints += turn.points
    turn.clear()
  }

  /** Ingests the contents of [day] into this player. */
  fun ingestDayAndCopyNextDay() {
    points += day.mountainPoints
    points += day.bestDayPoints
    points += day.apresPoints
    day.clear()
    day.copyFrom(nextDay)
    nextDay.clear()
  }

  /** Gains [cards] skill cards from the [skillDecks], then shuffles this player's [skillDeck]. */
  fun gainSkillCards(cards: List<Grade>, skillDecks: SkillDecks) {
    cards.forEach { skillDeck.add(skillDecks.draw(it)) }
    skillDeck.shuffle()
  }

  /** Gains the given [chips]. */
  fun gainTrainingChips(chips: List<TrainingChip>) {
    trainingChips.addAll(chips)
  }

  /** Gains [playerCard] and all of its associated benefits. */
  suspend fun gainPlayerCard(playerCard: PlayerCard, skillDecks: SkillDecks) {
    gainSkillCards(playerCard.skillCardsList, skillDecks)
    gainTrainingChips(playerCard.chipsList)
    playerCards.add(playerCard)
    if (playerCard.chooseChips > 0) {
      gainTrainingChips(handler.chooseChipsToGain(this, playerCard.chooseChips))
    }
  }
}

/** A mutable instance of a player's turn. */
class MutableTurn : Player.Turn {
  override var points = 0
  override var speed = 0

  /** Clears this mutable turn. */
  fun clear() {
    points = 0
    speed = 0
  }
}

/** A mutable instance of a player's turn. */
class MutableDay : Player.Day {
  override var mountainPoints = 0
  override var bestDayPoints = 0
  override var apresPoints = 0

  /** Clears this mutable experience. */
  fun clear() {
    mountainPoints = 0
    bestDayPoints = 0
    apresPoints = 0
  }

  /** Copies the contents of [other] into this. */
  fun copyFrom(other: MutableDay) {
    mountainPoints = other.mountainPoints
    bestDayPoints = other.bestDayPoints
    apresPoints = other.apresPoints
  }
}
