package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.player.proto.PlayerTraining
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks

/** Immutable access to a player object. */
interface Player {
  /** Applies the given mutation function to this player. */
  fun mutate(fn: MutablePlayer.() -> Unit)

  /** The input parameters of the player from the player card. */
  val playerCard: PlayerCard

  /** Hooks for this player's abilities. */
  val abilityHandler: AbilityHandler

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

  /** Returns true iff the player is on the mountain. */
  val isOnMountain: Boolean get() = location != null

  /** The apres link the player took this day. Null if still on mountain today. */
  val apresLink: Int?

  /** A single day for the player. */
  interface Day {
    /** How much experience this player has gained in the day. */
    val experience: Int

    /** A bonus for winning by a large amount. */
    data class OverkillBonus(val threshold: Int, val bonus: Int)

    /** The overkill bonus applied to this day, if any. */
    val overkillBonusPoints: OverkillBonus?

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

    /** How much unspent experience this player has gained in this turn. */
    val experience: Int

    /** How much speed the player has in this turn. */
    val speed: Int
  }

  /** The player's current turn, if any. */
  val turn: Turn

  /** The player's current day, if any. */
  val day: Day
}

/** Hooks for player abilities. */
open class AbilityHandler(open val player: Player) {
  /** Computes the added bonus for the given [tile]. */
  open fun computeBonus(tile: SlopeTile): Int = 0

  /** Called when the player should gain speed. Returns whether the player should gain speed. */
  open fun onGainSpeed(currentSpeed: Int): Boolean = true

  /** Called when the player crashes by [diff]. Returns whether the player should continue their turn. */
  open fun onCrash(gameModel: GameModel, diff: Int, isWipeout: Boolean) = false

  /** Called when the player gains points. */
  open fun onGainPoints(points: Int, gameModel: GameModel) {}

  /** Called when the player rests. */
  open fun onRest(gameModel: GameModel) {}

  /** Called at the start of a player's turn. */
  open fun onBeforeTurn(gameModel: GameModel) {}

  /** Called at the end of a player's turn. */
  open fun onAfterTurn(gameModel: GameModel) {}

  /** Returns the point multiplier to apply to apres points. */
  open fun getApresPointsMultiplier(): Int = 1

  /** Returns the multiplier for hazard training. */
  open fun getHazardTrainingMultiplier(): Int = 1

  /** Returns the additional grades that green training should apply to. */
  open fun getGreenTrainingBonusGrades(): List<Grade> = emptyList()

  open fun onRevealTopCard(card: Int) {}

  /** Returns whether the player ignores slow zones. */
  open fun ignoresSlowZones(): Boolean = false
}

/** Mutable access to a player object. */
class MutablePlayer(
  override val playerCard: PlayerCard,
  override val handler: PlayerHandler,
  abilityHandlerConstructor: (Player) -> AbilityHandler,
) :
  Player {
  override var points = 0; private set
  override var experience = 0; private set
  override var location: HexPoint? = null
  override var apresLink: Int? = null

  override val turn = MutableTurn()
  override val day = MutableDay()

  val nextDay = MutableDay()

  override val skillDeck = mutableListOf<Int>()
  override val skillDiscard = mutableListOf<Int>()

  override val training = mutableListOf(0, 0, 0)
  override val abilities = mutableListOf(false, false)

  override val abilityHandler = abilityHandlerConstructor(this)

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

  /** Shuffles all cards from discard back into skill deck. */
  fun refreshSkillDeck() {
    skillDeck.addAll(skillDiscard)
    skillDeck.shuffle()
    skillDiscard.clear()
  }

  /** Ingests the contents of [turn] into this player. */
  fun ingestTurn() {
    day.mountainPoints += turn.points
    day.experience += turn.experience
    turn.clear()
  }

  /** Ingests the contents of [day] into this player. */
  fun ingestDayAndCopyNextDay() {
    experience += day.experience
    points += day.mountainPoints
    points += day.bestDayPoints
    points += day.apresPoints
    day.clear()
    day.copyFrom(nextDay)
    nextDay.clear()
  }

  /** Buys the starting deck of cards. */
  fun buyStartingDeck(skillDecks: SkillDecks) {
    gainSkillCards(
      listOf(
        List(5) { Grade.GRADE_GREEN },
        List(3) { Grade.GRADE_BLUE },
        List(2) { Grade.GRADE_BLACK }).flatten(),
      skillDecks
    )
  }

  /** Buys the small upgrade from the player card. */
  fun buyUpgrade(index: Int, skillDecks: SkillDecks) {
    check(experience > 0) { "Need 1 experience, found $experience" }
    experience--
    gainSkillCards(playerCard.upgradesList[index].cardsList, skillDecks)
  }

  /** Gains [count] skill cards from the [skillDecks], then shuffles this player's [skillDeck]. */
  fun gainSkillCards(cards: List<Grade>, skillDecks: SkillDecks) {
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

  /** Computes the bonus this player gets against the given [tile]. */
  fun computeBonus(tile: SlopeTile): Int {
    return (0 until playerCard.trainingCount).sumOf { i ->
      val cardTraining = playerCard.trainingList[i]

      val applies = when (cardTraining.typeCase) {
        PlayerTraining.TypeCase.GRADE -> tile.grade == cardTraining.grade
          || (cardTraining.grade == Grade.GRADE_GREEN
          && tile.grade in abilityHandler.getGreenTrainingBonusGrades())

        PlayerTraining.TypeCase.CONDITION -> tile.condition == cardTraining.condition
        PlayerTraining.TypeCase.HAZARD -> tile.hazardsList.contains(cardTraining.hazard)
        PlayerTraining.TypeCase.TYPE_NOT_SET, null -> throw IllegalArgumentException("Invalid training type")
      }

      if (applies) {
        training[i] * cardTraining.value * (if (cardTraining.typeCase == PlayerTraining.TypeCase.HAZARD) abilityHandler.getHazardTrainingMultiplier() else 1)
      } else {
        0
      }
    } + abilityHandler.computeBonus(tile)
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

/** A mutable instance of a player's turn. */
class MutableDay : Player.Day {
  override var overkillBonusPoints: Player.Day.OverkillBonus? = null
  override var experience = 0
  override var mountainPoints = 0
  override var bestDayPoints = 0
  override var apresPoints = 0

  /** Clears this mutable experience. */
  fun clear() {
    overkillBonusPoints = null
    experience = 0
    mountainPoints = 0
    bestDayPoints = 0
    apresPoints = 0
  }

  /** Copies the contents of [other] into this. */
  fun copyFrom(other: MutableDay) {
    overkillBonusPoints = other.overkillBonusPoints
    experience = other.experience
    mountainPoints = other.mountainPoints
    bestDayPoints = other.bestDayPoints
    apresPoints = other.apresPoints
  }
}