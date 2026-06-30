package com.redpup.justsendit.model.random

import com.redpup.justsendit.model.proto.Die


/** Utilities for operating on [Die]. */
object Dice {

  /** Returns the maximum value of [this]. */
  val Die.maxValue: Int
    get() = when (this) {
      Die.DIE_GREEN -> 4
      Die.DIE_BLUE -> 6
      Die.DIE_BLACK -> 8
      Die.DIE_UNSET, Die.UNRECOGNIZED -> throw IllegalArgumentException("No max value for $this")
    }

  /** Rolls this die using the given [random]. */
  fun Die.roll(random: Random) = random.nextInt(maxValue) + 1

  /** Upgrades this to the next best die. */
  fun Die.upgrade() = when (this) {
    Die.DIE_GREEN -> Die.DIE_BLUE
    Die.DIE_BLUE, Die.DIE_BLACK -> Die.DIE_BLACK
    Die.DIE_UNSET, Die.UNRECOGNIZED -> throw IllegalArgumentException("Cannot upgrade die $this")
  }

  /** Downgrades this to the next worst die. */
  fun Die.downgrade() = when (this) {
    Die.DIE_GREEN, Die.DIE_BLUE -> Die.DIE_GREEN
    Die.DIE_BLACK -> Die.DIE_BLUE
    Die.DIE_UNSET, Die.UNRECOGNIZED -> throw IllegalArgumentException("Cannot downgrade die $this")
  }
}