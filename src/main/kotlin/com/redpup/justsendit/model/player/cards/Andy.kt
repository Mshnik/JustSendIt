package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Andy(val player: Player) : AbilityHandler {

  override suspend fun onCrash(gameModel: GameModel, diff: Int, isWipeout: Boolean): Boolean {
    return super.onCrash(gameModel, diff, isWipeout) || player.abilities[0] && diff >= -2
  }

  override suspend fun onGainSpeed(currentSpeed: Int): Boolean {
    if (player.abilities[1] && currentSpeed == 2) {
      player.mutate {
        turn.points += 5
      }
    }
    return super.onGainSpeed(currentSpeed)
  }
}
