package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Dannver(override val player: Player) : AbilityHandler(player) {

  override fun onCrash(gameModel: GameModel, diff: Int) {
    if (player.abilities[0]) {
      player.mutate {
        turn.experience += 1
      }
    }
    super.onCrash(gameModel, diff)
  }

  override fun onGainPoints(points: Int, gameModel: GameModel) {
    if (player.abilities[1] && points >= 5) {
      player.mutate {
        turn.points += 2
      }
    }
    super.onGainPoints(points, gameModel)
  }
}
