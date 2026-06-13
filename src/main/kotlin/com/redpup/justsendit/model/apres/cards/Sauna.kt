package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random

class Sauna(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
    random: Random,
  ) {
    val pointsPerUniqueIcon = if (isFirstPlayerToArrive) 6 else 3
    val uniqueIcons =
      player.playerCards.mapNotNull { if (it.proto.hasIcon()) it.proto.icon else null }.distinct()
        .count()
    player.points += uniqueIcons * pointsPerUniqueIcon
  }
}
