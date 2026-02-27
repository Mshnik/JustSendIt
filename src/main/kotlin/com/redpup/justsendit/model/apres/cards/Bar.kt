package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class Bar(override val apresCard: ApresCard) : Apres {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    player.refreshSkillDeck()
    if (isFirstPlayerToArrive) {
      player.day.apresPoints += player.revealCards(5)
    } else {
      player.day.apresPoints += player.revealCards(3)
    }
  }

  private fun MutablePlayer.revealCards(cards: Int): Int {
    return (1..cards).sumOf { playSkillCard() ?: 0 }
  }
}
