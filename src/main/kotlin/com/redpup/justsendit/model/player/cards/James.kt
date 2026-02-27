package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class James(val player: Player) : AbilityHandler {

    override fun onGainPoints(points: Int, gameModel: GameModel) {
        // groomer cruiser
        if (player.abilities[1]) {
            val tile = gameModel.tileMap[player.location!!]
            if (tile != null && tile.hasSlope() && tile.slope.condition == Condition.CONDITION_GROOMED) {
                player.mutate { turn.points += 2 }
            }
        }
    }

    override suspend fun onSuccessfulRun(gameModel: GameModel, diff: Int) {
        if (player.abilities[0] && diff >= 7) {
            if (player.handler.shouldGainSpeed(player)) {
                player.mutate { turn.speed++ }
            }
        }
    }
}
