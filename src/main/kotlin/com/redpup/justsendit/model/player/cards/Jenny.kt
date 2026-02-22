package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player

class Jenny(override val player: Player) : AbilityHandler(player) {

    private var enduranceUsedThisDay = false

    override fun onBeforeTurn(gameModel: GameModel) {
        if (gameModel.clock.turn == 1) {
            enduranceUsedThisDay = false
        }
    }

    override fun onCrash(gameModel: GameModel, diff: Int, isWipeout: Boolean): Boolean {
        if (player.abilities[0] && !enduranceUsedThisDay && isWipeout) {
            if (player.handler.decideToUseEndurance()) {
                enduranceUsedThisDay = true
                // Discard hand and redo. The game logic for redoing a turn is complex.
                // The hook returns a boolean to continue the turn.
                return true
            }
        }
        return super.onCrash(gameModel, diff, isWipeout)
    }

    override fun onGainPoints(points: Int, gameModel: GameModel) {
        // powder princess
        if (player.abilities[1]) {
            val tile = gameModel.tileMap[player.location!!]
            if (tile != null && tile.hasSlope() && tile.slope.condition == Condition.CONDITION_POWDER) {
                player.mutate { turn.points += 2 }
            }
        }
    }
}
