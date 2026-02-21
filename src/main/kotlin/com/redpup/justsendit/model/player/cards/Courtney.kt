package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Courtney(override val player: Player) : AbilityHandler(player) {

  override fun ignoresSlowZones(): Boolean = player.abilities[0] || super.ignoresSlowZones()

  override fun onGainPoints(points: Int, gameModel: GameModel) {
    val mountainTile = gameModel.tileMap[player.location!!]
    if (player.abilities[1]
      && mountainTile != null
      && mountainTile.hasSlope()
      && mountainTile.slope.hasTerrainPark()
    ) {
      player.mutate {
        turn.points += 4
      }
    }
    super.onGainPoints(points, gameModel)
  }
}
