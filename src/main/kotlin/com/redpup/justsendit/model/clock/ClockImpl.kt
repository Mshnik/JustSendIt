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
  override val onStateChanged = mutableListOf<(GameState, GameState) -> Unit>()

  private fun checkState(expected: GameState) {
    check(state == expected) { "Expected $expected, found $state" }
  }

  private fun changeState(from: GameState, to: GameState) {
    checkState(from)
    state = to
    onStateChanged.forEach { it(from, to) }
  }

  override fun startGame() {
    changeState(GameState.BEFORE_START, GameState.BETWEEN_DAYS)

    day = Day.DAY_FRIDAY
    round = 1
    turn = 1
    subTurn = 1
  }

  override fun startDay() {
    changeState(GameState.BETWEEN_DAYS, GameState.BETWEEN_ROUNDS)

    round = 1
    turn = 1
    subTurn = 1
  }

  override fun endDay(): Boolean {
    changeState(
      GameState.AFTER_LAST_ROUND_OF_DAY,
      if (day == Day.DAY_SUNDAY) GameState.AFTER_LAST_DAY else GameState.BETWEEN_DAYS
    )

    round = 1
    turn = 1
    subTurn = 1

    day = when (day) {
      Day.DAY_FRIDAY -> Day.DAY_SATURDAY
      Day.DAY_SATURDAY -> Day.DAY_SUNDAY
      Day.DAY_SUNDAY -> Day.DAY_GAME_OVER
      Day.DAY_BEFORE_START, Day.DAY_GAME_OVER, Day.DAY_UNSET, Day.UNRECOGNIZED -> throw IllegalStateException()
    }

    return day != Day.DAY_GAME_OVER
  }

  override fun startRound() {
    changeState(GameState.BETWEEN_ROUNDS, GameState.BETWEEN_TURNS)

    turn = 1
    subTurn = 1
  }

  override fun endRound(maxRound: Int) {
    changeState(
      GameState.AFTER_LAST_TURN_OF_ROUND,
      if (round + 1 < maxRound) GameState.BETWEEN_ROUNDS else GameState.AFTER_LAST_ROUND_OF_DAY
    )

    round++
    turn = 1
    subTurn = 1
  }

  override fun startTurn() {
    changeState(GameState.BETWEEN_TURNS, GameState.TURN_IN_PROGRESS)
  }

  override fun endTurn(turnsRemain: Boolean) {
    changeState(
      GameState.TURN_IN_PROGRESS,
      if (turnsRemain) GameState.BETWEEN_TURNS else GameState.AFTER_LAST_TURN_OF_ROUND
    )

    turn++
    subTurn = 1
  }

  override fun incrementSubTurn() {
    checkState(GameState.TURN_IN_PROGRESS)

    subTurn++
  }
}
