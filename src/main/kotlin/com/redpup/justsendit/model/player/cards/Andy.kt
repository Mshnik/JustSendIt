package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Andy(override val player: Player) : AbilityHandler(player) {

  override fun onCrash(gameModel: GameModel, diff: Int) {
    if (player.abilities[0] && diff >= -2) {
      player.mutate {
        turn.speed++
      }
    }
    super.onCrash(gameModel, diff)
  }

  override fun onGainSpeed(currentSpeed: Int): Boolean {
    if (player.abilities[1] && currentSpeed == 2) {
      player.mutate {
        turn.points += 5
      }
    }
    return super.onGainSpeed(currentSpeed)
  }
}
