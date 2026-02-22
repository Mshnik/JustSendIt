package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.proto.Grade

class Mia(override val player: Player) : AbilityHandler(player) {

    override fun getGreenTrainingBonusGrades(): List<Grade> {
        return if (player.abilities[0]) {
            listOf(Grade.GRADE_BLUE, Grade.GRADE_BLACK)
        } else {
            super.getGreenTrainingBonusGrades()
        }
    }

    override fun onAfterTurn(gameModel: GameModel) {
        if (player.abilities[1]) {
            player.mutate {
                turn.points += player.turn.speed
            }
        }
        super.onAfterTurn(gameModel)
    }
}
