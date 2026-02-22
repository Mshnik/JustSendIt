package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.util.pop
import javax.inject.Singleton

/** Implementation of [ApresDeck]. */
@Singleton
@VisibleForTesting
class FakePlayerDeck @Inject constructor() : PlayerDeck {
  var cards = mutableListOf<PlayerCard>()

  /** Draws the top card from the Player deck. */
  override fun draw(): PlayerCard = cards.pop("Player Deck")

  /** Resets the Apres deck. */
  override fun reset() {
    cards.clear()
  }
}