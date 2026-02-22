package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.proto.Grade

class Michael(override val player: Player) : AbilityHandler(player) {

    private val gradesThisTurn = mutableListOf<Grade>()

    override fun onBeforeTurn(gameModel: GameModel) {
        gradesThisTurn.clear()
        // know before you go
        if (player.abilities[0]) {
            val topCard = player.skillDeck.firstOrNull()
            if (topCard != null) {
                player.handler.onRevealTopCard(topCard)
            }
        }
        super.onBeforeTurn(gameModel)
    }

    override fun onGainPoints(points: Int, gameModel: GameModel) {
        // all rounder
        if (player.abilities[1]) {
            val tile = gameModel.tileMap[player.location!!]
            if (tile != null && tile.hasSlope()) {
                val grade = tile.slope.grade
                if (grade !in gradesThisTurn) {
                    player.mutate { turn.points += 2 }
                    gradesThisTurn.add(grade)
                }
            }
        }
    }
}
