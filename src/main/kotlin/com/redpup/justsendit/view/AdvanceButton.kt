package com.redpup.justsendit.view

import com.redpup.justsendit.model.proto.GameState
import javafx.application.Platform
import javafx.scene.control.Button
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Main button that advances game state, as needed in certain states. */
class AdvanceButton(
  private val guiState: GuiState,
) : Button() {
  internal val listeners = mutableListOf<suspend () -> Unit>()
  private var confirmDeferred: CompletableDeferred<Unit>? = null

  init {
    styleClass.add("phase-button")
  }

  /** Sets up the button for confirmation. */
  fun setupConfirm(deferred: CompletableDeferred<Unit>) {
    Platform.runLater {
      this.confirmDeferred = deferred
      text = "CONFIRM"
      isDisable = true // Start disabled until selection is made
      setOnAction {
        deferred.complete(Unit)
        this.confirmDeferred = null
      }
    }
  }

  /** Sets up the button with [label] and [onAction]. Does nothing if the label is already [label]. */
  private fun setup(label: String, onAction: suspend CoroutineScope.() -> Unit) {
    isDisable = false
    if (text == label) {
      return
    }
    text = label

    setOnAction {
      guiState.coroutineScope.launch {
        onAction()
        listeners.forEach { it() }
      }.invokeOnCompletion {
        when (guiState.gameModel.state) {
          GameState.GAME_STATE_UNSET, GameState.UNRECOGNIZED -> throw IllegalStateException()
          GameState.BEFORE_START -> setupStartGame()
          GameState.BETWEEN_DAYS -> setupStartDay()
          GameState.BETWEEN_ROUNDS -> setupStartRound()
          GameState.BETWEEN_TURNS, GameState.TURN_IN_PROGRESS -> setupTurn()
          GameState.AFTER_LAST_TURN_OF_ROUND -> setupEndRound()
          GameState.AFTER_LAST_ROUND_OF_DAY -> setupEndDay()
          GameState.AFTER_LAST_DAY -> setupEndGame()
        }
      }
    }
  }

  override fun disarm() {
    super.disarm()
    isDisable = true
  }

  /** Sets up the AdvanceButton to be a "Start Game" button. */
  fun setupStartGame() {
    setup("Start Game") {
      guiState.gameModel.startGame()
    }
  }

  /** Sets up the AdvanceButton to be a "Next Turn" button. */
  private fun setupTurn() {
    setup("Next Turn") {
      guiState.gameModel.turn()
    }
  }

  /** Sets up the AdvanceButton to be a "Start Round" button. */
  private fun setupStartRound() {
    setup("Start Round") {
      guiState.gameModel.startRound()
    }
  }

  /** Sets up the AdvanceButton to be a "End Round" button. */
  private fun setupEndRound() {
    setup("End Round") {
      guiState.gameModel.endRound()
    }
  }

  /** Sets up the AdvanceButton to be a "Start Day" button. */
  private fun setupStartDay() {
    setup("Start Day") {
      guiState.gameModel.startDay()
    }
  }

  /** Sets up the AdvanceButton to be a "End Day" button. */
  private fun setupEndDay() {
    setup("End Day") {
      guiState.gameModel.endDay()
    }
  }

  /** Sets up the end of game. */
  private fun setupEndGame() {
    text = "Game Over"
    disarm()
  }
}