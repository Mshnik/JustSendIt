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

  fun log(log: Log) {
    logContainer.children.add(Label(log.format()))
  }

  private fun Log.format(): String {
    val event = when (eventCase) {
      Log.EventCase.MOUNTAIN_DECISION -> "$playerName chose $mountainDecision"
      Log.EventCase.PLAYER_MOVE -> "$playerName moved from ${playerMove.from} to ${playerMove.to}"
      Log.EventCase.SKILL_CARD_DRAW -> "$playerName drew skill card(s) with values ${skillCardDraw.cardValueList}"
      else -> "Unknown event"
    }
    return "[$day/$turn/$subturn] $event"
  }
}
