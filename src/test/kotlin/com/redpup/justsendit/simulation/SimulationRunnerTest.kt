package com.redpup.justsendit.simulation

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.model.proto.GameState
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class SimulationRunnerTest {

  @Test
  fun run_executesFullGame() = runBlocking {
    val gameModel = mock<MutableGameModel>()
    
    // Define state transitions
    whenever(gameModel.state).thenReturn(
      GameState.BETWEEN_TURNS, // Turn 1
      GameState.BETWEEN_ROUNDS, // End Round 1
      GameState.BETWEEN_DAYS, // End Day 1
      GameState.BEFORE_START // Game ends (or state is unexpected)
    )
    
    whenever(gameModel.advanceDay()).thenReturn(true, false)

    val runner = SimulationRunner(gameModel)
    runner.run()

    verify(gameModel).startGame()
    verify(gameModel, atLeastOnce()).turn()
    verify(gameModel, atLeastOnce()).endRound()
    verify(gameModel, atLeastOnce()).advanceDay()
    
    // Verify results were printed (indirectly by ensuring it finishes)
    assertThat(gameModel.state).isEqualTo(GameState.BEFORE_START)
  }
}
