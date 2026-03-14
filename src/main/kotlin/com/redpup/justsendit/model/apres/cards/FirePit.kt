package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class FirePit(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    val pointsPerUnique = if (isFirstPlayerToArrive) 4 else 2
    val unique = player.skillDiscard.distinct().count()
    player.day.apresPoints += unique * pointsPerUnique
  }
}
