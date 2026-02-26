package com.redpup.justsendit.view.log

import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.GameModel
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox

class LogPanel(private val gameModel: GameModel) : ScrollPane() {
    private val logContainer = VBox()

    init {
        content = logContainer
        isFitToWidth = true
    }

    fun update() {
        logContainer.children.clear()
        gameModel.logs.forEach { log ->
            logContainer.children.add(Label(formatLog(log)))
        }
    }

    private fun formatLog(log: Log): String {
        val event = when (log.eventCase) {
            Log.EventCase.PLAYER_CHOICE -> {
                val choice = log.playerChoice
                "${choice.playerName} chose ${choice.decision}"
            }
            Log.EventCase.PLAYER_MOVE -> {
                val move = log.playerMove
                "${move.playerName} moved from ${move.from} to ${move.to}"
            }
            Log.EventCase.SKILL_CARD_DRAW -> {
                val draw = log.skillCardDraw
                "${draw.playerName} drew a skill card with value ${draw.cardValue}"
            }
            else -> "Unknown event"
        }
        return "[${log.timestamp}] $event"
    }
}
