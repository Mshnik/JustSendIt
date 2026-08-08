package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.skill.calculation.SkillCalculationUtilities.dieColorOrWild
import com.redpup.justsendit.model.supply.proto.GainEffect
import com.redpup.justsendit.model.supply.proto.Icon
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardEffect

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
  val SkillCardEffect.EFFECT_FACTOR: Double
    get() = when (conditionCase) {
      SkillCardEffect.ConditionCase.SUCCESS -> 0.8
      else -> 1.0
    }

  /** Cost to add to [SkillCardEffect] value. Will be 0 if none or negative if present. */
  val SkillCardEffect.EFFECT_COST: Double
    get() = when (conditionCase) {
      SkillCardEffect.ConditionCase.DISCARD_CARD -> -3.501888449
      else -> 0.0
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

// TODO: Make class with resolution.
object SkillCardEvResolvedValues {
  /** C15 "Card Draw": value of drawing a card. */
  val CARD_DRAW = 4.723558411

  /** C16 "Card Filter (2)": value of replacing the worst of two cards with another. */
  val CARD_FILTER_2 = 0.956443446

  /** C17 "Card Filter (3)": value of replacing the worst of three cards with another. */
  val CARD_FILTER_3 = 1.434665169

  /** C66 "Trash card (Deck/Discard)": value of one point of `GainEffect.trashes`. */
  val TRASH_CARD_DECK_DISCARD = 1.972240652


  /**
   * "Look at the top 3 cards of your deck, put 1 on top, discard the others" combo value
   * (C17). Recognized from three consecutive `card_effect` entries:
   *   TOPDECK -> REVEALED_TOPDECK (count 3),
   *   REVEALED_TOPDECK -> DISCARD (count 2),
   *   REVEALED_TOPDECK -> TOPDECK (count 1).
   */
  val LOOK_AT_TOP_3_KEEP_1 = CARD_FILTER_3

  /**
   * "Draw 2 cards, then put 2 cards on top of your deck" combo value (2 x C16). Recognized
   * from two consecutive `card_effect` entries:
   *   TOPDECK -> HAND (count 2),
   *   HAND -> TOPDECK (count 2).
   */
  val DRAW_2_TOPDECK_2 = 2 * CARD_FILTER_2

  /**
   * Flat value for `filter_hand` ("discard any number of cards, then draw that many"). The
   * sheet's version wasn't scaled by hand size (C16 + C17), and per your direction this stays
   * flat rather than becoming hand-size-aware.
   */
  val FILTER_HAND = CARD_FILTER_2 + CARD_FILTER_3

  /**
   * C4 "Effect EV": AVERAGE(Cards!S2:S1036), the average effect EV across every card in the
   * sheet. Used as the flat value for `reactivate_following` ("activate the effect of the
   * card below an additional time"), per your direction to hardcode this constant.
   */
  val REACTIVATE_FOLLOWING = 2.200542645
}