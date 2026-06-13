package com.redpup.justsendit.model

import java.util.*

/** Types of dice that can be rolled. */
enum class DieType {
  GREEN, BLUE, BLACK;

  /** Rolls this die using the given [random]. */
  fun roll(random: Random) = when (this) {
    GREEN -> random.nextInt(4) + 1
    BLUE -> random.nextInt(6) + 1
    BLACK -> random.nextInt(8) + 1
  }

  /** Upgrades this to the next best die. */
  fun upgrade() = when (this) {
    GREEN -> BLUE
    BLUE -> BLACK
    BLACK -> BLACK
  }

  /** Downgrades this to the next worst die. */
  fun downgrade() = when (this) {
    GREEN -> GREEN
    BLUE -> GREEN
    BLACK -> BLUE
  }
}