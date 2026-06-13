package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random

class PhotoOp(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
    random: Random,
  ) {
    val pointsPerSpeed = if (isFirstPlayerToArrive) 6 else 3
    // TODO
    // player.points += player.turn.speed * pointsPerSpeed
  }
}
