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
          GameState.BEFORE_START -> setupStart()
          GameState.BETWEEN_DAYS -> setupDay()
          GameState.AFTER_LAST_TURN, GameState.BETWEEN_ROUNDS -> setupRound()
          GameState.TURN_IN_PROGRESS, GameState.BETWEEN_TURNS -> setupTurn()
          GameState.AFTER_END -> setupEnd()
          GameState.GAME_STATE_UNSET, GameState.UNRECOGNIZED -> throw IllegalStateException()
        }
      }
    }
  }

  override fun disarm() {
    super.disarm()
    isDisable = true
  }

  /** Sets up the AdvanceButton to be a "Start Game" button. */
  fun setupStart() {
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

  /** Sets up the AdvanceButton to be a "Next Round" button. */
  private fun setupRound() {
    setup("Next Round") {
      guiState.gameModel.endRound()
    }
  }

  /** Sets up the AdvanceButton to be a "Next Day" button. */
  private fun setupDay() {
    setup("Next Day") {
      guiState.gameModel.advanceDay()
    }
  }

  /** Sets up the end of game. */
  private fun setupEnd() {
    text = "Game Over"
    disarm()
  }
}