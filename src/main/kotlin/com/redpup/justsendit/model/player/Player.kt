package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.skill.Skill

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
  val controller: PlayerController

  /** How many points (fun) this player has. */
  val points: Int

  /** The player's current skill deck, in order. */
  val skillDeck: List<Skill>

  /** The player's current skill discard, unordered. */
  val skillDiscard: Collection<Skill>

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

  /** The player's current hand. */
  val hand: List<Skill>

  /** The skill cards the player has in play this round. */
  val inPlay: List<Skill>

  /** The player's current wobbles. */
  val wobbles: Int
}

/** Hooks for player abilities. */
interface AbilityHandler {}

/** Mutable access to a player object. */
class MutablePlayer(override val controller: PlayerController) : Player {
  override val playerCards = mutableListOf<PlayerCard>()
  override val name: String get() = playerCards.firstOrNull()?.name ?: "No Name"
  override var points = 0
    set(points) {
      field = points
      field.coerceAtLeast(0)
    }

  override var location: HexPoint? = null
  override var apresLink: Int? = null

  override val turn = MutableTurn()
  override val day = MutableDay()
  val nextDay = MutableDay()

  override val hand = mutableListOf<Skill>()
  override val inPlay = mutableListOf<Skill>()
  override val skillDeck = mutableListOf<Skill>()
  override val skillDiscard = mutableListOf<Skill>()
  override var wobbles = 0; internal set

  override val abilityHandler = object : AbilityHandler {}

  override fun toString() = name

  /** Applies [fn] to this player. */
  override fun mutate(fn: MutablePlayer.() -> Unit) {
    this.fn()
  }

  /** Plays [card] from [hand] into [inPlay]. */
  fun playCard(card: Skill) {
    check(hand.remove(card))
    inPlay.add(card)
  }

  /** Plays the top card of the skill deck, returning it and putting it in the discard. */
  fun playSkill(): Skill? {
    val card = skillDeck.removeFirstOrNull()
    if (card != null) {
      skillDiscard.add(card)
    }
    return card
  }

  /** Adds [count] wobbles to this player. */
  fun addWobbles(count: Int) {
    wobbles += count
  }

  /** Resets wobbles to 0. */
  fun resetWobbles() {
    wobbles = 0
  }

  /** Draws [count] cards from the deck into hand. */
  fun drawCards(count: Int) {
    for (i in 1..count) {
      val card = skillDeck.removeFirstOrNull()
      if (card != null) {
        hand.add(card)
      } else {
        // Shuffle discard into deck and try again
        refreshDecks()
        val cardAfterShuffle = skillDeck.removeFirstOrNull()
        if (cardAfterShuffle != null) {
          hand.add(cardAfterShuffle)
        }
      }
    }
  }

  /** Shuffles all cards from discard back into skill deck. */
  fun refreshDecks() {
    skillDeck.addAll(skillDiscard)
    skillDeck.shuffle()
    skillDiscard.clear()
  }

  /** Discards [card] from [hand]. */
  fun discardFromHand(card: Skill) {
    check(hand.remove(card))
    skillDiscard.add(card)
  }

  /** Discards all [inPlay] cards. */
  fun discardInPlay() {
    skillDiscard.addAll(inPlay)
    inPlay.clear()
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

  /** Gains [card] skill card, then shuffles this player's [skillDeck]. */
  fun gainSkill(card: Skill) {
    this.skillDeck.add(card)
    this.skillDeck.shuffle()
  }

  /** Gains [playerCard] and all of its associated benefits. */
  fun gainPlayerCard(playerCard: PlayerCard) {
    playerCards.add(playerCard)
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
