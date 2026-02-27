package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.GameModel

class Journal(override val apresCard: ApresCard) : Apres {
  override suspend fun apply(
      player: MutablePlayer,
      isFirstPlayerToArrive: Boolean,
      gameModel: GameModel,
  ) {
    val unlockedAbilities = player.abilities.count { it }
    if (isFirstPlayerToArrive) {
      player.day.apresPoints += unlockedAbilities * 10
    } else {
      player.day.apresPoints += unlockedAbilities * 5
    }
  }
}
