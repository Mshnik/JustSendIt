package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.supply.proto.PlayerCard as PlayerCardProto
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.util.pop
import javax.inject.Singleton

/** Implementation of [ApresDeck]. */
@Singleton
@VisibleForTesting
class FakePlayerDeck @Inject constructor(private val playerFactory: PlayerFactory) : PlayerDeck {
  private val cards = buildMap {
    put(Day.DAY_FRIDAY, mutableListOf())
    put(Day.DAY_SATURDAY, mutableListOf())
    put(Day.DAY_SUNDAY, mutableListOf<PlayerCardProto>())
  }

  /** Adds the given [cards] to this. */
  fun add(vararg cards: PlayerCardProto) {
    cards.forEach {
      check(it.day in this.cards.keys) { "${it.day} not found in ${this.cards}" }
      this.cards[it.day]!!.add(it)
    }
  }

  /** Draws the top card from the Player deck. */
  override fun draw(day: Day): PlayerCard = playerFactory.create(cards[day]!!.pop("Player Deck"))

  /** Resets the player deck. */
  override fun reset() {
    cards.values.forEach { it.clear() }
  }
}