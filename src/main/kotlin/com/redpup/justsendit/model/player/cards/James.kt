package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class James(override val player: Player) : AbilityHandler(player) {

  // "iron skis": "when you beat difficulty by 7 or more, may gain 1 more speed"
  // This would be checked where overkill is calculated.
  // The player can have a hook for when an overkill bonus is applied.

  override fun onAfterTurn(gameModel: GameModel) {
    // "groomer cruiser": "gain 2 points on groomers"
    // This should be based on tiles traversed during the turn.
    // GameModel would need to track this.
    super.onAfterTurn(gameModel)
  }
}
