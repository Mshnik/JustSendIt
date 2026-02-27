package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class Village(override val apresCard: ApresCard) : Apres {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      player.day.apresPoints += player.skillDeck.size
    } else {
      player.day.apresPoints += player.skillDeck.size / 2
    }
  }
}
