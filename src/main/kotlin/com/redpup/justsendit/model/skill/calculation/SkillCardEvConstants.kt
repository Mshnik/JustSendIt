package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.skill.calculation.SkillCalculationUtilities.dieColorOrWild
import com.redpup.justsendit.model.supply.proto.*

/**
 * TODO: Description
 */
object SkillCardEvConstants {
  /** C98 "Lift/Pass Comparison Factor": used when combining dice/icon EV with effect EV. */
  const val LIFT_PASS_COMPARISON_FACTOR = 0.1

  /** EV of an [Icon]. */
  val Icon.EV: Double
    get() = when (typeCase) {
      Icon.TypeCase.WILD -> 1.0
      Icon.TypeCase.TYPE_NOT_SET -> 0.0
      // TODO: Change value based on icon.
      else -> 0.25
    }

  // ---- Effect Calculations tab ----

  /** C51 "Prevent Wobble": value of `ignore_wobble`. */
  const val PREVENT_WOBBLE = 3.0

  /** C68 "Refresh Shop": value of `replenish_shop`. */
  const val REFRESH_SHOP = 1.0

  /** C75 "Additional immediate turn": value of `extra_turn`. */
  const val ADDITIONAL_TURN = 1.5

  /** C47 "Buy": value of one point of `GainEffect.buys`. */
  const val BUY = 2.0

  /** C38 "Fun" (Points): value of one point of `GainEffect.fun`. */
  const val POINTS = 0.6

  /** Factor to apply to [SkillCard] effect values based on [category]. */
  val SkillCard.TIMING_FACTOR: Double
    get() = when (category) {
      EffectCategory.EFFECT_CATEGORY_PLAY -> 1.0
      EffectCategory.EFFECT_CATEGORY_FIRST -> 0.8
      EffectCategory.EFFECT_CATEGORY_LAST -> 0.8
      EffectCategory.EFFECT_CATEGORY_PASS -> 1.0
      EffectCategory.EFFECT_CATEGORY_LIFT -> 1.0
      // A card with effects but no category is a data error in the source sheet too
      // (its AG column would evaluate to the literal string "None"). Treat as 0 EV.
      else -> 0.0
    }

  /** Factor to apply to a [SkillCardEffect] value based on [conditionCase]. */
  val SkillCardEffectCondition.EFFECT_FACTOR: Double
    get() = when (conditionCase) {
      SkillCardEffectCondition.ConditionCase.SUCCESS -> 0.8
      SkillCardEffectCondition.ConditionCase.FAILURE -> 0.2
      SkillCardEffectCondition.ConditionCase.MATCHING_ICON -> TODO()
      SkillCardEffectCondition.ConditionCase.CONDITION_NOT_SET -> 0.0
      null -> throw IllegalStateException()
    }

  /** Cost to add to [SkillCardEffect] value. Will be 0 if none or negative if present. */
  val SkillCardEffectCost.EFFECT_COST: Double
    get() = when (costCase) {
      SkillCardEffectCost.CostCase.DISCARD_CARD -> -3.501888449
      SkillCardEffectCost.CostCase.COST_NOT_SET -> 0.0
      null -> throw IllegalStateException()
    }

  /** Factor applied to a [GainEffect]'s value based on its [EFFECT_REPEAT_FACTOR]. */
  val GainEffect.EFFECT_REPEAT_FACTOR: Double
    get() = when (repeatCase) {
      GainEffect.RepeatCase.SKILL_CARD_ABOVE -> 0.9
      GainEffect.RepeatCase.SKILL_CARD_BELOW -> 0.9
      GainEffect.RepeatCase.WOBBLE -> 1.2
      GainEffect.RepeatCase.MATCHING_DIE -> when (matchingDie.dieColorOrWild()) {
        Die.DIE_GREEN -> 0.5
        Die.DIE_BLUE -> 0.4
        Die.DIE_BLACK -> 0.3
        // Null is wild.
        null -> 0.8
        else -> throw IllegalStateException("Unexpected die type.")
      }

      else -> 1.0
    }

  /** Value of rerolling a die. */
  val Die?.REROLL_VALUE: Double
    get() = when (this) {
      Die.DIE_GREEN -> 1.333333333
      Die.DIE_BLUE -> 2.0
      Die.DIE_BLACK -> 2.666666667
      // Unset die in a reroll effect is wild.
      else -> 3.0
    }

  /** Value of nudging a die. */
  val Die?.NUDGE_VALUE: Double
    get() = when (this) {
      Die.DIE_GREEN -> 1.5
      Die.DIE_BLUE -> 1.5
      Die.DIE_BLACK -> 1.5
      else -> 2.0
    }
}
