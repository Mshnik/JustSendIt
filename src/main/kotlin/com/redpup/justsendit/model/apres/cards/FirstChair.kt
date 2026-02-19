package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.GameModel

class FirstChair(override val apresCard: ApresCard) : Apres {
  override fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      // TODO: 2 bonus turns.
    } else {
      // TODO: 1 bonus turns.
    }
  }
}
