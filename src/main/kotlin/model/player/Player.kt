package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks

/** Immutable access to a player object. */
interface Player {
  /** The input parameters of the player from the player card. */
  val playerCard: PlayerCard

  /** The handler for making decisions. */
  val handler: PlayerHandler

  /** How many points (fun) this player has. */
  val points: Int

  /** How much unspent experience this player has. */
  val experience: Int

  /** The player's current skill deck, in order. */
  val skillDeck: List<Int>

  /** The player's current skill discard, unordered. */
  val skillDiscard: Collection<Int>

  /** The levels of training the player has for each of their training types. */
  val training: List<Int>

  /** The abilities the player has unlocked. */
  val abilities: List<Boolean>

  /** The player's current location on the mountain. Null if not on mountain. */
  val location: HexPoint?

  /** A single turn for the player. */
  interface Turn {
    /** How many points (fun) this player has gained in this turn. */
    val points: Int

    /** How much unspent experience this player has gained in this turn. */
    val experience: Int

    /** How much speed the player has in this turn. */
    val speed: Int
  }

  /** The player's current turn, if any. */
  val turn: Turn
}

/** Mutable access to a player object. */
class MutablePlayer(override val playerCard: PlayerCard, override val handler: PlayerHandler) :
  Player {
  override var points = 0
  override var experience = 0
  override val skillDeck = mutableListOf<Int>()
  override val skillDiscard = mutableListOf<Int>()
  override val training = mutableListOf(0, 0, 0)
  override val abilities = mutableListOf(false, false)
  override var location: HexPoint? = null
  override var turn = MutableTurn()

  /** Plays the top card of the skill deck, returning it and putting it in the discard. */
  fun playSkillCard(): Int? {
    val card = skillDeck.removeFirstOrNull()
    if (card != null) {
      skillDiscard.add(card)
    }
    return card
  }

  /** Shuffles all cards from discard back into skill deck. */
  fun refreshSkillDeck(): Unit {
    skillDeck.addAll(skillDiscard)
    skillDeck.shuffle()
    skillDiscard.clear()
  }

  /** Ingests the contents of [turn] into this player. */
  fun ingestTurn() {
    points += turn.points
    experience += turn.experience
    turn.clear()
  }

  /** Buys the starting deck of cards. */
  fun buyStartingDeck(skillDecks: SkillDecks) {
    gainSkillCards(
      listOf(List(5) { Grade.GRADE_GREEN },
             List(3) { Grade.GRADE_BLUE },
             List(2) { Grade.GRADE_BLACK }).flatten(),
      skillDecks
    )
  }

  /** Buys the small upgrade from the player card. */
  fun buySmallUpgrade(skillDecks: SkillDecks) {
    check(experience > 0) { "Need 1 experience, found $experience" }
    experience--
    gainSkillCards(playerCard.smallUpgradeList, skillDecks)
  }

  /** Buys the large upgrade from the player card. */
  fun buyLargeUpgrade(skillDecks: SkillDecks) {
    check(experience > 0) { "Need 1 experience, found $experience" }
    experience--
    gainSkillCards(playerCard.largeUpgradeList, skillDecks)
  }

  /** Gains [count] skill cards from the [skillDecks], then shuffles this player's [skillDeck]. */
  private fun gainSkillCards(cards: List<Grade>, skillDecks: SkillDecks) {
    cards.forEach { skillDeck.add(skillDecks.draw(it)) }
    skillDeck.shuffle()
  }

  /** Buys the given index of training. */
  fun buyTraining(index: Int) {
    check(experience > 0) { "Need 1 experience, found $experience" }
    experience--
    training[index]++
  }

  /** Buys the given index of ability. */
  fun buyAbility(index: Int) {
    val cost = playerCard.abilitiesList[index].cost
    check(!abilities[index]) { "Ability $index is already unlocked" }
    check(experience >= cost) { "Need $cost experience, found $experience" }
    experience -= cost
    abilities[index] = true
  }
}

/** A mutable instance of a player's turn. */
class MutableTurn : Player.Turn {
  override var points = 0
  override var experience = 0
  override var speed = 0

  /** Clears this mutable turn. */
  fun clear() {
    points = 0
    experience = 0
    speed = 0
  }
}