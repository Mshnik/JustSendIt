package com.redpup.justsendit.model.apres.cards

import com.google.common.collect.Range
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random

class Lodge(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
    random: Random,
  ) {
    val count = if (isFirstPlayerToArrive) 2 else 1
    val otherApres = gameModel.apres.filter { it != this }
    player.controller.chooseApresCard(gameModel, player, otherApres, Range.closed(count, count))
      .forEach { it.apply(player, false, gameModel, random) }
  }
}
