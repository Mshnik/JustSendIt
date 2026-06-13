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
  private var baseCards: List<SkillCard> = emptyList()

  /** Adds all [cards] to [cards] and updates [baseCards]. */
  fun add(vararg cards: SkillCard) {
    this.cards.addAll(cards)
    this.baseCards = this.cards.toList()
  }

  override fun reset() {
    cards.clear()
    cards.addAll(baseCards)
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
