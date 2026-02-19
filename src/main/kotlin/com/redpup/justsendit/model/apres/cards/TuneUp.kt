package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.GameModel

class TuneUp(override val apresCard: ApresCard) : Apres {
  override fun apply(
      player: MutablePlayer,
      isFirstPlayerToArrive: Boolean,
      gameModel: GameModel,
  ) {
    val maxToRemove = if (isFirstPlayerToArrive) 3 else 2
    val cardsToRemove = player.handler.chooseCardsToRemove(player, player.skillDeck, maxToRemove)
    cardsToRemove.forEach { player.skillDeck.remove(it) }
  }
}
