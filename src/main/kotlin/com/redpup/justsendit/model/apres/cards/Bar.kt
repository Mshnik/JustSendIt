package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.BaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.supply.SkillDecks.Companion.value

class Bar(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    player.refreshDecks()
    val numCards = if (isFirstPlayerToArrive) 6 else 3
    player.day.apresPoints += (1..numCards).mapNotNull { player.playSkillCard() }.sumOf { it.value }
  }
}
