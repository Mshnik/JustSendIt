package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Yifei(override val player: Player) : AbilityHandler(player) {

  override fun getHazardTrainingMultiplier(): Int =
    if (player.abilities[0]) 2 else super.getHazardTrainingMultiplier()

  override fun onGainPoints(points: Int, gameModel: GameModel) {
    val mountainTile = gameModel.tileMap[player.location!!]
    if (player.abilities[1] && mountainTile != null && mountainTile.hasSlope() && mountainTile.slope.hazardsCount > 0) {
      player.mutate { turn.points += 1 }
    }
    super.onGainPoints(points, gameModel)
  }
}
