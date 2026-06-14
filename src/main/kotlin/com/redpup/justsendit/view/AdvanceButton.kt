package com.redpup.justsendit.view

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.proto.GameState
import com.redpup.justsendit.view.info.GameInfoPanel
import javafx.scene.control.Button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Main button that advances game state, as needed in certain states. */
class AdvanceButton(
  private val guiState: GuiState,
  private val gameInfoPanel: GameInfoPanel,
) : Button(), Logger {

  override fun log(log: Log) {
    when (log.stateTransition) {
      GameState.BEFORE_START -> setupStart()
      GameState.BETWEEN_DAYS -> setupDay()
      GameState.BETWEEN_ROUNDS -> setupRound()
      GameState.BETWEEN_TURNS -> setupTurn()
      GameState.TURN_IN_PROGRESS -> disarm()
      GameState.GAME_STATE_UNSET, GameState.UNRECOGNIZED, null -> {}
    }
  }

  /** Sets up the button with [label] and [onAction]. Does nothing if the label is already [label]. */
  private fun setup(label: String, onAction: suspend CoroutineScope.() -> Unit) {
    arm()
    if (text == label) {
      return
    }
    text = label
    setOnAction { guiState.coroutineScope.launch { onAction() } }
  }

  /** Sets up the AdvanceButton to be a "Start Game" button. */
  fun setupStart() {
    setup("Start Game") {
      guiState.gameModel.startGame()
      gameInfoPanel.update()
    }
  }

  /** Sets up the AdvanceButton to be a "Next Turn" button. */
  fun setupTurn() {
    setup("Next Turn") {
      guiState.gameModel.turn()
      gameInfoPanel.update()
    }
  }

  /** Sets up the AdvanceButton to be a "Next Round" button. */
  fun setupRound() {
    setup("Next Round") {
      guiState.gameModel.endRound()
      gameInfoPanel.update()
    }
  }

  /** Sets up the AdvanceButton to be a "Next Day" button. */
  fun setupDay() {
    setup("Next Day") {
      guiState.gameModel.advanceDay()
      gameInfoPanel.update()
    }
  }
}