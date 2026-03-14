package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

class Sauna(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    val pointsPerUniqueChip = if (isFirstPlayerToArrive) 6 else 3
    player.day.apresPoints +=
      listOf(player.trainingChips, player.usedTrainingChips)
        .flatten()
        .distinct()
        .count() * pointsPerUniqueChip
  }
}
