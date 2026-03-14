package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks.Companion.getSkillGrade

class IceSkating(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    val pointsPerBlue = if (isFirstPlayerToArrive) 5 else 3
    val blues = player.skillDiscard.count { it.getSkillGrade() == Grade.GRADE_BLUE }
    player.day.apresPoints += blues * pointsPerBlue
  }
}
