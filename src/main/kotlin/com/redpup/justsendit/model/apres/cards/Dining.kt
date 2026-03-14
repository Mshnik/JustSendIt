package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class Dining(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    val pointsPerPair = if (isFirstPlayerToArrive) 7 else 4
    val pairs = player.skillDiscard.groupingBy { it }.eachCount().values.sumOf { it / 2 }
    player.day.apresPoints += pairs * pointsPerPair
  }
}
