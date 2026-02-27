package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.proto.Grade

class BuyGear(override val apresCard: ApresCard) : Apres {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      player.gainSkillCards(
        listOf(
          Grade.GRADE_BLUE,
          Grade.GRADE_BLUE,
          Grade.GRADE_BLACK,
          Grade.GRADE_BLACK,
          Grade.GRADE_BLACK,
          Grade.GRADE_BLACK
        ), gameModel.skillDecks
      )
    } else {
      player.gainSkillCards(
        listOf(
          Grade.GRADE_BLUE,
          Grade.GRADE_BLUE,
          Grade.GRADE_BLUE,
          Grade.GRADE_BLUE,
          Grade.GRADE_BLACK
        ), gameModel.skillDecks
      )
    }
  }
}
