package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player.Day.OverkillBonus

class Sauna(override val apresCard: ApresCard) : Apres {
  override fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      player.nextDay.overkillBonusPoints = OverkillBonus(5, 4)
    } else {
      player.nextDay.overkillBonusPoints = OverkillBonus(5, 2)
    }
  }
}
