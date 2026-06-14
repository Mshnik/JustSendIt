package com.redpup.justsendit.model.random

import com.redpup.justsendit.model.proto.Die


/** Utilities for operating on [Die]. */
object Dice {

  /** Rolls this die using the given [random]. */
  fun Die.roll(random: Random) = when (this) {
    Die.DIE_GREEN -> random.nextInt(4) + 1
    Die.DIE_BLUE -> random.nextInt(6) + 1
    Die.DIE_BLACK -> random.nextInt(8) + 1
    Die.DIE_UNSET, Die.UNRECOGNIZED -> throw IllegalArgumentException("Cannot roll die $this")
  }

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