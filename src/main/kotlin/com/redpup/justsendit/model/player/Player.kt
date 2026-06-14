package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.random.Random
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

  /** The player's current wobbles. */
  val wobbles: Int

  /** The player's current hand. */
  val hand: List<Skill>

  /** The skill cards the player has in play this round. */
  val inPlay: List<Skill>

  /** The player's current skill deck, in order. */
  val skillDeck: List<Skill>

  /** The player's current skill discard, unordered. */
  val skillDiscard: Collection<Skill>

  /** The player's current location on the mountain. Null if not on mountain. */
  val location: HexPoint?

  /** True iff the player has passed this round. */
  val isPassed: Boolean

  /** Returns true iff the player is on the mountain. */
  val isOnMountain: Boolean get() = location != null

  /** The apres link the player took this day. Null if still on mountain today. */
  val apresLink: Int?
}

/** Hooks for player abilities. */
interface AbilityHandler {}

/** Mutable access to a player object. */
class MutablePlayer(override val controller: PlayerController) : Player {
  override val playerCards = mutableListOf<PlayerCard>()
  override val name: String
    get() = playerCards.firstOrNull()?.name ?: controller.name
  override var points = 0
    set(points) {
      field = points
      field.coerceAtLeast(0)
    }

  override var location: HexPoint? = null
  override var apresLink: Int? = null
  override var isPassed: Boolean = false

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
  fun drawCards(count: Int, random: Random) {
    for (i in 1..count) {
      val card = skillDeck.removeFirstOrNull()
      if (card != null) {
        hand.add(card)
      } else {
        // Shuffle discard into deck and try again
        refreshDecks(random)
        val cardAfterShuffle = skillDeck.removeFirstOrNull()
        if (cardAfterShuffle != null) {
          hand.add(cardAfterShuffle)
        }
      }
    }
  }

  /** Shuffles all cards from discard back into skill deck. */
  fun refreshDecks(random: Random) {
    skillDeck.addAll(skillDiscard)
    random.shuffle(skillDeck)
    skillDiscard.clear()
  }

  /** Discards [card] from [hand]. */
  fun discardFromHand(vararg card: Skill) {
    check(hand.removeAll(card))
    skillDiscard.addAll(card)
  }

  /** Discard all [hand] cards. */
  fun discardHand() {
    skillDiscard.addAll(hand)
    hand.clear()
  }

  /** Discards all [inPlay] cards. */
  fun discardInPlay() {
    skillDiscard.addAll(inPlay)
    inPlay.clear()
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
