package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Yifei(val player: Player) : AbilityHandler {

    override fun getHazardTrainingMultiplier(): Int {
        return if (player.abilities[0]) 2 else super.getHazardTrainingMultiplier()
    }

    override fun onGainPoints(points: Int, gameModel: GameModel) {
        if (player.abilities[1]) {
            val tile = gameModel.tileMap[player.location!!]
            if (tile != null && tile.hasSlope() && tile.slope.hazardsCount > 0) {
                player.mutate { turn.points += 1 }
            }
        }
    }
}
