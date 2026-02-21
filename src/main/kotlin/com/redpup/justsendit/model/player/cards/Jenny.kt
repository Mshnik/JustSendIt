package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Jenny(override val player: Player) : AbilityHandler(player) {

  override fun onCrash(gameModel: GameModel, diff: Int) {
    if (player.abilities[0]) {
      // "once each day when you crash you may discard hand and redo (same card count)"
      // Needs handler decision and game logic support.
    }
    super.onCrash(gameModel, diff)
  }

  // "powder princess": "gain 2 points on powder "
  // Similar to James' "groomer cruiser", needs info about tiles traversed.
}
