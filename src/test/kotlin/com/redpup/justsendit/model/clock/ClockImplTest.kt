package com.redpup.justsendit.model.clock

import com.google.common.truth.Truth.assertWithMessage
import com.google.inject.Guice
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.proto.GameState
import javax.inject.Inject
import kotlin.test.BeforeTest
import org.junit.jupiter.api.Test

class ClockImplTest {

  @Inject private lateinit var clock: Clock

  @BeforeTest
  fun setup() {
    Guice.createInjector(ClockModule(2)).injectMembers(this)
  }

  @Test
  fun `ClockImpl default values`() {
    clock.assertExpected(
      Expectation(GameState.BEFORE_START, Day.DAY_BEFORE_START, 1, 1, 1)
    )
  }

  @Test
  fun `progress clock sets values`() {
    PROGRESSION.forEachIndexed { index, progression ->
      try {
        progression.first(clock)
      } catch (e: Throwable) {
        throw AssertionError("State failure during progression $index", e)
      }
      clock.assertExpected(progression.second, index)
    }
  }

  /** Wrapper on expected clock values. */
  data class Expectation(
    val state: GameState,
    val day: Day,
    val round: Int,
    val turn: Int,
    val subTurn: Int,
  )

  private fun Clock.assertExpected(expected: Expectation, index: Int = 0) {
    assertWithMessage("Progression $index").that(state).isEqualTo(expected.state)
    assertWithMessage("Progression $index").that(day).isEqualTo(expected.day)
    assertWithMessage("Progression $index").that(round).isEqualTo(expected.round)
    assertWithMessage("Progression $index").that(turn).isEqualTo(expected.turn)
    assertWithMessage("Progression $index").that(subTurn).isEqualTo(expected.subTurn)
  }

  private companion object {
    val startGame: Clock.() -> Unit = { startGame() }
    val startDay: Clock.() -> Unit = { startDay() }
    val endDay: Clock.() -> Unit = { endDay() }
    val startRound: Clock.() -> Unit = { startRound() }
    val endRound: Clock.() -> Unit = { endRound(999) }
    val endLastRoundOfDay: Clock.() -> Unit = { endRound(0) }
    val startTurn: Clock.() -> Unit = { startTurn() }
    val endTurn: Clock.() -> Unit = { endTurn(true) }
    val endLastTurnOfRound: Clock.() -> Unit = { endTurn(false) }
    val incrementSubTurn: Clock.() -> Unit = { incrementSubTurn() }

    val PROGRESSION = listOf(
      startGame to Expectation(GameState.BETWEEN_DAYS, Day.DAY_FRIDAY, 1, 1, 1),
      startDay to Expectation(GameState.BETWEEN_ROUNDS, Day.DAY_FRIDAY, 1, 1, 1),
      startRound to Expectation(GameState.BETWEEN_TURNS, Day.DAY_FRIDAY, 1, 1, 1),
      startTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 1, 1, 1),
      incrementSubTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 1, 1, 2),
      incrementSubTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 1, 1, 3),
      endTurn to Expectation(GameState.BETWEEN_TURNS, Day.DAY_FRIDAY, 1, 2, 1),
      startTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 1, 2, 1),
      incrementSubTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 1, 2, 2),
      endLastTurnOfRound to Expectation(
        GameState.AFTER_LAST_TURN_OF_ROUND,
        Day.DAY_FRIDAY,
        1,
        3,
        1
      ),
      endRound to Expectation(GameState.BETWEEN_ROUNDS, Day.DAY_FRIDAY, 2, 1, 1),
      startRound to Expectation(GameState.BETWEEN_TURNS, Day.DAY_FRIDAY, 2, 1, 1),
      startTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_FRIDAY, 2, 1, 1),
      endLastTurnOfRound to Expectation(
        GameState.AFTER_LAST_TURN_OF_ROUND,
        Day.DAY_FRIDAY,
        2,
        2,
        1
      ),
      endLastRoundOfDay to Expectation(GameState.AFTER_LAST_ROUND_OF_DAY, Day.DAY_FRIDAY, 3, 1, 1),
      endDay to Expectation(GameState.BETWEEN_DAYS, Day.DAY_SATURDAY, 1, 1, 1),
      startDay to Expectation(GameState.BETWEEN_ROUNDS, Day.DAY_SATURDAY, 1, 1, 1),
      startRound to Expectation(GameState.BETWEEN_TURNS, Day.DAY_SATURDAY, 1, 1, 1),
      startTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_SATURDAY, 1, 1, 1),
      endLastTurnOfRound to Expectation(
        GameState.AFTER_LAST_TURN_OF_ROUND,
        Day.DAY_SATURDAY,
        1,
        2,
        1
      ),
      endLastRoundOfDay to Expectation(GameState.AFTER_LAST_ROUND_OF_DAY, Day.DAY_SATURDAY, 2, 1, 1),
      endDay to Expectation(GameState.BETWEEN_DAYS, Day.DAY_SUNDAY, 1, 1, 1),
      startDay to Expectation(GameState.BETWEEN_ROUNDS, Day.DAY_SUNDAY, 1, 1, 1),
      startRound to Expectation(GameState.BETWEEN_TURNS, Day.DAY_SUNDAY, 1, 1, 1),
      startTurn to Expectation(GameState.TURN_IN_PROGRESS, Day.DAY_SUNDAY, 1, 1, 1),
      endLastTurnOfRound to Expectation(
        GameState.AFTER_LAST_TURN_OF_ROUND,
        Day.DAY_SUNDAY,
        1,
        2,
        1
      ),
      endLastRoundOfDay to Expectation(GameState.AFTER_LAST_ROUND_OF_DAY, Day.DAY_SUNDAY, 2, 1, 1),
      endDay to Expectation(GameState.AFTER_LAST_DAY, Day.DAY_GAME_OVER, 1, 1, 1),
    )
  }
}
