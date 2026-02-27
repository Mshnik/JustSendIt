package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Amy(val player: Player) : AbilityHandler {

  override fun getApresPointsMultiplier(): Int =
    if (player.abilities[1]) 2 else super.getApresPointsMultiplier()

  override suspend fun onGainSpeed(currentSpeed: Int): Boolean {
    if (player.abilities[0] && !player.handler.shouldGainSpeed(player)) {
      return false
    }
    return super.onGainSpeed(currentSpeed)
  }
}
