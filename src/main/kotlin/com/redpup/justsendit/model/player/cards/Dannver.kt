package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Dannver(val player: Player) : AbilityHandler {

  override suspend fun onCrash(gameModel: GameModel, diff: Int, isWipeout: Boolean): Boolean {
    if (player.abilities[0] && isWipeout) {
      player.mutate {
        turn.experience += 1
      }
    }
    return super.onCrash(gameModel, diff, isWipeout)
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
