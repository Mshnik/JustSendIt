package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class David(override val player: Player) :
  AbilityHandler(player) {

  override fun onRest(gameModel: GameModel) {
    if (player.abilities[0]) {
      val direction = player.handler.chooseMoveOnRest(player)
      if (direction != null) {
        player.mutate {
          location = location!! + direction
        }
      }
    }
    super.onRest(gameModel)
  }
}
