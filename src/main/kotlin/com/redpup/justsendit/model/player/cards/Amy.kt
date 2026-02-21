package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Amy(override val player: Player) : AbilityHandler(player) {

  override fun getApresPointsMultiplier(): Int =
    if (player.abilities[1]) 2 else super.getApresPointsMultiplier()

  override fun onGainSpeed(currentSpeed: Int): Boolean {
    if (player.abilities[0] && !player.handler.shouldGainSpeed(player)) {
      return false
    }
    return super.onGainSpeed(currentSpeed)
  }
}
