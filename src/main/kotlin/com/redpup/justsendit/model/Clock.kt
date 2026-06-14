package com.redpup.justsendit.model

import com.redpup.justsendit.model.proto.Day

/** Recording of time in game. */
interface Clock {
  /** What round of day it is. */
  val round: Int

  /** What sub-turn of the day it is. */
  val subTurn: Int

  /** Returns whether it is the first subTurn of the turn. */
  val isFirstSubTurn: Boolean get() = subTurn == 1

  /** The max round of this day. */
  val maxRound: Int

  /** What game of day it is. */
  val day: Day
}

/** Mutable instance of [Clock]. */
class MutableClock(override var round: Int = 1, override var day: Day = Day.DAY_FRIDAY) : Clock {
  /** Returns the max turn of the day. */
  override val maxRound: Int
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
  fun advanceRound() {
    round++
    resetSubTurn()
  }

  /** Advances to the next day. */
  fun advanceDay() {
    round = 1
    resetSubTurn()
    day = when (day) {
      Day.DAY_FRIDAY -> Day.DAY_SATURDAY
      Day.DAY_SATURDAY -> Day.DAY_SUNDAY
      Day.DAY_SUNDAY -> throw IllegalStateException("No day after sunday")
      Day.DAY_UNSET, Day.UNRECOGNIZED -> throw IllegalStateException()
    }
  }
}
