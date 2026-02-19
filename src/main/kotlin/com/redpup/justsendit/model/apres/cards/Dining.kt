package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.GameModel

class Dining(override val apresCard: ApresCard) : Apres {
  override fun apply(
      player: MutablePlayer,
      isFirstPlayerToArrive: Boolean,
      gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      player.points += player.skillDiscard.size
    } else {
      player.points += player.skillDiscard.size / 2
    }
  }
}
