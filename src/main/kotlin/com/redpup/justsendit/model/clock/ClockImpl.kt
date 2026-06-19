package com.redpup.justsendit.model.clock

import com.google.inject.Inject
import com.google.inject.Singleton
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.proto.GameState

/** Mutable instance of [Clock]. */
@Singleton
class ClockImpl @Inject constructor(
) : Clock {
  override var state: GameState = GameState.BEFORE_START
  override var day: Day = Day.DAY_BEFORE_START
  override var round: Int = 1
  override var turn: Int = 1
  override var subTurn: Int = 1

  private fun checkState(expected: GameState) {
    check(state == expected) { "Expected $expected, found $state" }
  }

  override fun startGame() {
    checkState(GameState.BEFORE_START)
    state = GameState.BETWEEN_DAYS

    day = Day.DAY_FRIDAY
    round = 1
    turn = 1
    subTurn = 1
  }

  override fun startDay() {
    checkState(GameState.BETWEEN_DAYS)
    state = GameState.BETWEEN_ROUNDS

    round = 1
    turn = 1
    subTurn = 1
  }

  override fun endDay(): Boolean {
    checkState(GameState.BETWEEN_ROUNDS)

    state = GameState.BETWEEN_DAYS
    round = 1
    turn = 1
    subTurn = 1

    day = when (day) {
      Day.DAY_FRIDAY -> Day.DAY_SATURDAY
      Day.DAY_SATURDAY -> Day.DAY_SUNDAY
      Day.DAY_SUNDAY -> {
        state = GameState.AFTER_END
        Day.DAY_GAME_OVER
      }

      Day.DAY_BEFORE_START, Day.DAY_GAME_OVER, Day.DAY_UNSET, Day.UNRECOGNIZED -> throw IllegalStateException()
    }

    return day != Day.DAY_GAME_OVER
  }

  override fun startRound() {
    checkState(GameState.BETWEEN_ROUNDS)
    state = GameState.BETWEEN_TURNS

    turn = 1
    subTurn = 1
  }

  override fun endRound() {
    checkState(GameState.BETWEEN_TURNS)

    state = GameState.BETWEEN_ROUNDS

    round++
    turn = 1
    subTurn = 1
  }

  override fun startTurn() {
    checkState(GameState.BETWEEN_TURNS)

    state = GameState.TURN_IN_PROGRESS
  }

  override fun endTurn() {
    checkState(GameState.TURN_IN_PROGRESS)

    state = GameState.BETWEEN_TURNS

    turn++
    subTurn = 1
  }

  override fun incrementSubTurn() {
    checkState(GameState.TURN_IN_PROGRESS)

    subTurn++
  }
}
