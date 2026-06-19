package com.redpup.justsendit.view.sidebar

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.GameModel
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox

class LogPanel : ScrollPane(), Logger {
  private val logContainer = VBox()

  init {
    content = logContainer
    isFitToWidth = true
  }

  override fun log(log: Log) {
    logContainer.children.add(Label(log.format()))
  }

  private fun Log.format(): String {
    val event = when (eventCase) {
      Log.EventCase.STATE_TRANSITION -> "State: $stateTransition"
      Log.EventCase.MOUNTAIN_DECISION -> "$playerName chose $mountainDecision"
      Log.EventCase.PLAYER_MOVE -> "$playerName moved from ${
        playerMove.from.toString().replace("\n", " ").trim()
      } to ${playerMove.to.toString().replace("\n", " ").trim()}"

      Log.EventCase.SKI_RIDE_ATTEMPT -> "$playerName played ${skiRideAttempt.cardName}\n" +
        "  Total Difficulty: ${skiRideAttempt.totalTileDifficulty}\n" +
        "  Rolls: ${skiRideAttempt.rolledValuesList}\n" +
        "  Bonus: ${skiRideAttempt.totalIconValue}\n" +
        "  Total Skill (Cumulative): ${skiRideAttempt.cumulativeSkill}\n" +
        "  Total Wobbles (Cumulative): ${skiRideAttempt.cumulativeWobbles}\n" +
        "  Success: ${skiRideAttempt.success}"

      Log.EventCase.SKI_RIDE_CRASH -> "$playerName crashed: ${skiRideCrash.cause}"

      else -> "Unknown event"
    }
    return "[$day/$turn/$subturn] $event"
  }
}
