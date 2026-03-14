package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class Lodge(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    val count = if (isFirstPlayerToArrive) 2 else 1
    val otherApres = gameModel.apres.filter { it != this }
    player.controller.chooseOtherApres(player, otherApres, count)
      .forEach { it.apply(player, false, gameModel) }
  }
}
