package com.redpup.justsendit.model

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
  val day: Int

  object Params {
    const val MAX_DAY = 3
  }
}

/** Mutable instance of [Clock]. */
class MutableClock(override var turn: Int = 1, override var day: Int = 1) : Clock {
  /** Returns the max turn of the day. */
  override val maxTurn: Int
    get() = when (day) {
      1 -> 9
      2 -> 8
      3 -> 7
      else -> 0
    }

  override var subTurn: Int = 1; private set

  /** Advances to the next subturn. */
  fun advanceSubTurn() {
    subTurn++
  }

  /** Advances to the next turn. */
  fun advanceTurn() {
    turn++
    subTurn = 1
  }

  /** Advances to the next day. */
  fun advanceDay() {
    turn = 1
    subTurn = 1
    day++
  }
}
