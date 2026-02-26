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
      Log.EventCase.MOUNTAIN_DECISION -> "${log.playerName} chose ${log.mountainDecision}"
      Log.EventCase.PLAYER_MOVE -> "${log.playerName} moved from ${log.playerMove.from} to ${log.playerMove.to}"
      Log.EventCase.SKILL_CARD_DRAW -> "${log.playerName} drew skill card(s) with values ${log.skillCardDraw.cardValueList}"
      else -> "Unknown event"
    }
    return "[${log.timestamp}] $event"
  }
}
