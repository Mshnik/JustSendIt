package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.supply.proto.*

/**
 * TODO: Description
 */
object Constants {
  /** Minimum value an upgrade card can cost. */
  const val MIN_UPGRADE_COST = 2

  /** Maximum value an upgrade card can cost. */
  const val MAX_UPGRADE_COST = 7

  /** C98 "Lift/Pass Comparison Factor": used when combining dice/icon EV with effect EV. */
  const val LIFT_PASS_COMPARISON_FACTOR = 0.1

  /** C51 "Prevent Wobble": value of `ignore_wobble`. */
  const val PREVENT_WOBBLE = 3.0

  /** C68 "Refresh Shop": value of `replenish_shop`. */
  const val REFRESH_SHOP = 0.8

  /** C75 "Additional immediate turn": value of `extra_turn`. */
  const val ADDITIONAL_TURN = 1.5

  /** Value of a matching icon. */
  const val ICON = 2.0

  /** C47 "Buy": value of one point of `GainEffect.buys`. */
  const val BUY = 1.0

  /** C38 "Fun" (Points): value of one point of `GainEffect.fun`. */
  const val POINTS = 1.2

  /** Multiplicative value of getting to pick any kind of die. */
  const val WILD_DIE_PICK_FACTOR = 1.5

  /** Base value of being able to ignore a hazard. */
  const val HAZARD_IGNORE_VALUE = 4.0

  /** Factor to apply to [SkillCard] effect values based on [category]. */
  val SkillCard.TIMING_FACTOR: Double
    get() = when (category) {
      EffectCategory.EFFECT_CATEGORY_PLAY -> 1.0
      EffectCategory.EFFECT_CATEGORY_RIDE -> 0.8
      EffectCategory.EFFECT_CATEGORY_FINALE -> 0.8
      EffectCategory.EFFECT_CATEGORY_REST -> 0.8
      EffectCategory.EFFECT_CATEGORY_LIFT -> 0.2
      EffectCategory.EFFECT_CATEGORY_UNSET, EffectCategory.UNRECOGNIZED, null -> throw IllegalStateException()
    }

  /** Value of rerolling a die. */
  val Die?.REROLL_VALUE: Double
    get() = when (this) {
      Die.DIE_GREEN -> 1.333333333
      Die.DIE_BLUE -> 2.0
      Die.DIE_BLACK -> 2.666666667
      // Unset die in a reroll effect is wild.
      else -> 2.0 * WILD_DIE_PICK_FACTOR
    }

  /** Value of nudging a die. */
  val Die?.NUDGE_VALUE: Double
    get() = when (this) {
      Die.DIE_GREEN -> 1.5
      Die.DIE_BLUE -> 1.5
      Die.DIE_BLACK -> 1.5
      else -> 1.5 * WILD_DIE_PICK_FACTOR
    }

  /** Value of replacing dice. */
  // TODO: Maybe this needs more logic? But a naive replacing on a random card
  // is worse than what a player would do.
  val ReplaceDiceEffect.VALUE: Double get() = 2.0

  /** Value of moving a tile on the map. */
  const val MOVE_TILE: Double = 1.0
}
