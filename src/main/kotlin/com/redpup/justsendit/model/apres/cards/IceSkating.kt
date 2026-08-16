package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.random.Random

class IceSkating(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
    random: Random,
  ) {
    val pointsPerBlue = if (isFirstPlayerToArrive) 5 else 3
    val blues =
      player.skillDiscard.sumOf { skill -> skill.skillCard.diceList.count { it == Die.DIE_BLUE } }
    player.points += blues * pointsPerBlue
  }
}
