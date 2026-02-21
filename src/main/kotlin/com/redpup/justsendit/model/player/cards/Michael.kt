package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Michael(override val player: Player) : AbilityHandler(player) {

  override fun onBeforeTurn(gameModel: GameModel) {
    if (player.abilities[0]) {
      val topCard = player.skillDeck.firstOrNull()
      if (topCard != null) {
        onRevealTopCard(topCard)
      }
    }
    super.onBeforeTurn(gameModel)
  }

  // "all rounder ": "gain 2 points if the grade is different from all other grades this turn "
  // Needs tracking of grades ridden during the turn.
}
