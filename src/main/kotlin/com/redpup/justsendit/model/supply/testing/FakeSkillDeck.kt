package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.supply.SkillDeck
import com.redpup.justsendit.model.supply.proto.SkillCard
import javax.inject.Inject
import javax.inject.Singleton

/** A testing fake implementation of [SkillDeck]. */
@VisibleForTesting
@Singleton
class FakeSkillDeck @Inject constructor() : SkillDeck {
  private var cards: MutableList<SkillCard> = mutableListOf()

  /** Adds all [cards] to [cards]. */
  fun add(vararg cards: SkillCard) {
    this.cards.addAll(cards)
  }

  override fun reset() {
    cards.clear()
  }

  override fun draw(): SkillCard {
    return cards.removeFirst()
  }

  override fun find(name: String): SkillCard {
    val card = cards.find { it.name == name } ?: throw IllegalArgumentException("No card $name in $cards")
    cards.remove(card)
    return card
  }
}
