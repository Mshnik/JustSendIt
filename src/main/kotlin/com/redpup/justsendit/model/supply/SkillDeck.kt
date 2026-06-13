package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardList
import com.redpup.justsendit.util.TextProtoReaderImpl
import javax.inject.Qualifier

/** Access to skill decks in the supply. */
interface SkillDeck {
  /** Resets this skill decks to its starting state. */
  fun reset()

  /** Draws the top card of the deck. */
  fun draw(): SkillCard

  /** Finds and removes the [SkillCard] with the given [name]. */
  fun find(name: String): SkillCard
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
class SkillDeckInstance(path: String) : SkillDeck {
  private val reader = TextProtoReaderImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    shuffle = true
  )
  val cards = reader().toMutableList()

  init {
    reset()
  }

  override fun reset() {
    cards.clear()
    cards.addAll(reader())
  }

  override fun draw(): SkillCard = cards.removeFirst()

  override fun find(name: String) = cards.find { it.name == name }!!.also { cards.remove(it) }
}