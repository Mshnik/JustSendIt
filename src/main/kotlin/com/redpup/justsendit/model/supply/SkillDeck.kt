package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.supply.proto.SkillCardList
import com.redpup.justsendit.util.TextProtoReaderImpl
import javax.inject.Qualifier

/** Access to skill decks in the supply. */
interface SkillDeck {
  /** Resets this skill decks to its starting state. */
  fun reset()

  /** Draws the top card of the deck. */
  fun draw(): Skill

  /** Finds and removes the [Skill] with the given [name]. */
  fun find(name: String): Skill
}

/** [Qualifier] for the starter deck. */
@Qualifier
annotation class StarterDeck

/** [Qualifier] for the shop deck. */
@Qualifier
annotation class ShopDeck

/** [Qualifier] for the special deck. */
@Qualifier
annotation class SpecialDeck

/** The skill decks available for interaction in the supply. */
class SkillDeckInstance(
  path: String,
  shuffler: Random,
  private val skillFactory: SkillFactory,
) : SkillDeck {
  private val reader = TextProtoReaderImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    shuffler
  )
  val cards = reader().toMutableList()

  init {
    reset()
  }

  override fun reset() {
    cards.clear()
    cards.addAll(reader())
  }

  override fun draw(): Skill = skillFactory.create(cards.removeFirst())

  override fun find(name: String): Skill {
    val card =
      cards.find { it.name == name } ?: throw IllegalArgumentException("No card $name in $cards")
    cards.remove(card)
    return skillFactory.create(card)
  }
}