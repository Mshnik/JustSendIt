package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.util.pop
import javax.inject.Singleton

/** Implementation of [ApresDeck]. */
@Singleton
@VisibleForTesting
class FakePlayerDeck @Inject constructor() : PlayerDeck {
  private val cards = buildMap<Day, MutableList<PlayerCard>> {
    Day.DAY_FRIDAY to mutableListOf<PlayerCard>()
    Day.DAY_SATURDAY to mutableListOf<PlayerCard>()
    Day.DAY_SUNDAY to mutableListOf<PlayerCard>()
  }

  /** Adds the given [cards] to this. */
  fun add(vararg cards: PlayerCard) {
    cards.forEach { this.cards[it.day]!!.add(it) }
  }

  /** Draws the top card from the Player deck. */
  override fun draw(day: Day): PlayerCard = cards[day]!!.pop("Player Deck")

  /** Resets the Apres deck. */
  override fun reset() {
    cards.values.forEach { it.clear() }
  }
}