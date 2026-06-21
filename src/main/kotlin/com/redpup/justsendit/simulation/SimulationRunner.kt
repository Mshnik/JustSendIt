package com.redpup.justsendit.simulation

import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.model.proto.GameState
import javax.inject.Inject

/** Drives a [MutableGameModel] through a full game. */
class SimulationRunner @Inject constructor(private val gameModel: MutableGameModel) {

  /** Runs the simulation until completion. */
  suspend fun run() {
    println("Starting Simulation...")
    gameModel.startGame()

    var gameActive = true
    while (gameActive) {
      when (gameModel.state) {
        GameState.UNRECOGNIZED, GameState.GAME_STATE_UNSET -> throw IllegalStateException()

        GameState.BEFORE_START -> gameModel.startGame()
        GameState.BETWEEN_DAYS -> gameModel.startDay()
        GameState.BETWEEN_ROUNDS -> gameModel.startRound()
        GameState.BETWEEN_TURNS -> gameModel.turn()
        GameState.TURN_IN_PROGRESS -> {
          // This state is managed internally by gameModel.turn()
          // If we are here, something might be stuck or we just need to wait.
        }

        GameState.AFTER_LAST_TURN_OF_ROUND -> gameModel.endRound()
        GameState.AFTER_LAST_ROUND_OF_DAY -> gameModel.endDay()
        GameState.AFTER_LAST_DAY -> {
          gameActive = false
        }
      }
    }

    println("Simulation Finished!")
    printResults()
  }

  private fun printResults() {
    println("--- Final Results ---")
    val sortedPlayers = gameModel.players.sortedByDescending { it.points }
    for ((index, player) in sortedPlayers.withIndex()) {
      println("${index + 1}. ${player.name} (${player.controller.name}): ${player.points} points")
    }
  }
}
