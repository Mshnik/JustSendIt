package com.redpup.justsendit.model

import com.redpup.justsendit.model.proto.Day

/** Recording of time in game. */
interface Clock {
  /** What turn of day it is. */
  val turn: Int

  /** What sub-turn of the day it is. */
  val subTurn: Int

  /** Returns whether it is the first subTurn of the turn. */
  val isFirstSubTurn: Boolean get() = subTurn == 1

  /** The max turn of this day. */
  val maxTurn: Int

  /** What game of day it is. */
  val day: Day

  /** Returns the maximum number of cards that can be played on a day. */
  val maxCards: Int get() = day.number
}

/** Mutable instance of [Clock]. */
class MutableClock(override var turn: Int = 1, override var day: Day = Day.DAY_FRIDAY) : Clock {
  /** Returns the max turn of the day. */
  override val maxTurn: Int
    get() = when (day) {
      Day.DAY_FRIDAY -> 9
      Day.DAY_SATURDAY -> 8
      Day.DAY_SUNDAY -> 7
      Day.DAY_UNSET, Day.UNRECOGNIZED -> throw IllegalArgumentException()
    }

  override var subTurn: Int = 1; private set

  /** Advances to the next subturn. */
  fun advanceSubTurn() {
    subTurn++
  }

  /** Resets the subturn. */
  fun resetSubTurn() {
    subTurn = 1
  }

  /** Advances to the next turn. */
  fun advanceTurn() {
    turn++
    resetSubTurn()
  }

  /** Advances to the next day. */
  fun advanceDay() {
    turn = 1
    resetSubTurn()
    day = when (day) {
      Day.DAY_FRIDAY -> Day.DAY_SATURDAY
      Day.DAY_SATURDAY -> Day.DAY_SUNDAY
      Day.DAY_SUNDAY -> throw IllegalStateException("No day after sunday")
      Day.DAY_UNSET, Day.UNRECOGNIZED -> throw IllegalStateException()
    }
  }
}
