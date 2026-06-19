package com.redpup.justsendit.view

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.proto.GameState
import javafx.application.Platform
import javafx.scene.control.Button
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Main button that advances game state, as needed in certain states. */
class AdvanceButton(
  private val guiState: GuiState,
) : Button(), Logger {
  internal val listeners = mutableListOf<() -> Unit>()
  private var confirmDeferred: CompletableDeferred<Unit>? = null
  private var lastLog: Log? = null

  init {
    styleClass.add("phase-button")
  }

  override fun log(log: Log) {
    lastLog = log
    if (confirmDeferred != null) return // Don't update if in confirm mode

    when (log.stateTransition) {
      GameState.BEFORE_START -> setupStart()
      GameState.BETWEEN_DAYS -> setupDay()
      GameState.BETWEEN_ROUNDS -> setupRound()
      GameState.BETWEEN_TURNS -> setupTurn()
      GameState.TURN_IN_PROGRESS -> disarm()
      GameState.GAME_STATE_UNSET, GameState.UNRECOGNIZED, GameState.AFTER_END, null -> {}
    }
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
        // Restore last state if possible
        lastLog?.let { log(it) } ?: disarm()
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
    setOnAction { guiState.coroutineScope.launch { onAction() } }
  }

  override fun disarm() {
    super.disarm()
    isDisable = true
  }

  /** Sets up the AdvanceButton to be a "Start Game" button. */
  fun setupStart() {
    setup("Start Game") {
      guiState.gameModel.startGame()
      listeners.forEach { it() }
    }
  }

  /** Sets up the AdvanceButton to be a "Next Turn" button. */
  fun setupTurn() {
    setup("Next Turn") {
      guiState.gameModel.turn()
      listeners.forEach { it() }
    }
  }

  /** Sets up the AdvanceButton to be a "Next Round" button. */
  fun setupRound() {
    setup("Next Round") {
      guiState.gameModel.endRound()
      listeners.forEach { it() }
    }
  }

  /** Sets up the AdvanceButton to be a "Next Day" button. */
  fun setupDay() {
    setup("Next Day") {
      guiState.gameModel.advanceDay()
      listeners.forEach { it() }
    }
  }
}