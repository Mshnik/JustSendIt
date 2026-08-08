package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.REACTIVATE_FOLLOWING
import com.redpup.justsendit.model.supply.proto.GainEffect
import com.redpup.justsendit.model.supply.proto.Icon
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardEffect

/**
 * Constants mirroring the "Effect Calculations" and "Calculations" tabs of the card-balance
 * spreadsheet (Redesign.xlsx), used by [SkillCardEvComputer] to recompute [SkillCard.Computed].
 *
 * A few of these are themselves fleet-wide aggregates over every card in the spreadsheet
 * (e.g. [REACTIVATE_FOLLOWING], which is "Effect EV" = AVERAGE(Cards!S2:S1036)) rather than
 * values derivable from a single card. Those are hardcoded snapshots of the sheet's current
 * values and will drift out of date if the card set changes meaningfully — refresh them from
 * the "Effect Calculations" tab periodically.
 */
object SkillCardEvConstants {
  // ---- Calculations tab: die sides, by color. ----
  const val GREEN_DIE_SIDES = 4.0
  const val BLUE_DIE_SIDES = 6.0
  const val BLACK_DIE_SIDES = 8.0

  /** EV of an [Icon]. */
  val Icon.EV: Double
    get() = when (typeCase) {
      Icon.TypeCase.WILD -> 1.0
      Icon.TypeCase.TYPE_NOT_SET -> 0.0
      // TODO: Change value based on icon.
      else -> 0.25
    }

  // ---- Effect Calculations tab ----

  /** C15 "Card Draw": value of drawing a card. */
  const val CARD_DRAW = 4.723558411

  /** C16 "Card Filter (2)": value of replacing the worst of two cards with another. */
  const val CARD_FILTER_2 = 0.956443446

  /** C17 "Card Filter (3)": value of replacing the worst of three cards with another. */
  const val CARD_FILTER_3 = 1.434665169

  /** C51 "Prevent Wobble": value of `ignore_wobble`. */
  const val PREVENT_WOBBLE = 3.0

  /** C68 "Refresh Shop": value of `replenish_shop`. */
  const val REFRESH_SHOP = 1.0

  /** C75 "Additional immediate turn": value of `extra_turn`. */
  const val ADDITIONAL_TURN = 1.5

  /** C47 "Buy": value of one point of `GainEffect.buys`. */
  const val BUY = 2.0

  /** C66 "Trash card (Deck/Discard)": value of one point of `GainEffect.trashes`. */
  const val TRASH_CARD_DECK_DISCARD = 1.972240652

  /** C38 "Fun": value of one point of `GainEffect.fun`. */
  const val FUN = 0.6

  /**
   * C79-C82 "Numbered {Green,Blue,Black,Wild}": discount applied to a `GainEffect` whose
   * `repeat` is `matching_die` (i.e. it only triggers on a specific rolled value).
   */
  const val NUMBERED_GREEN = 0.5
  const val NUMBERED_BLUE = 0.4
  const val NUMBERED_BLACK = 0.3
  const val NUMBERED_WILD = 0.8


  /** Factor to apply to [SkillCard] effect values based on [category]. */
  val SkillCard.timingFactor: Double
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
  val SkillCardEffect.factor: Double
    get() = when (conditionCase) {
      SkillCardEffect.ConditionCase.SUCCESS -> 0.8
      else -> 1.0
    }

  val SkillCardEffect.cost: Double
    get() = when (conditionCase) {
      SkillCardEffect.ConditionCase.DISCARD_CARD -> -3.501888449
      else -> 0.0
    }

  /** Factor applied to a [GainEffect]'s value based on its [repeatFactor]. */
  val GainEffect.repeatFactor: Double
    get() = when (repeatCase) {
      GainEffect.RepeatCase.SKILL_CARD_ABOVE -> 0.9
      GainEffect.RepeatCase.SKILL_CARD_BELOW -> 0.9
      GainEffect.RepeatCase.WOBBLE -> 1.2
      GainEffect.RepeatCase.MATCHING_DIE -> 1.0
      else -> 1.0
    }

  /** Value of rerolling a die. */
  val Die?.rerollValue: Double
    get() = when (this) {
      Die.DIE_GREEN -> 1.333333333
      Die.DIE_BLUE -> 2.0
      Die.DIE_BLACK -> 2.666666667
      // Unset die in a reroll effect is wild.
      else -> 3.0
    }

  /** Value of nudging a die. */
  val Die?.nudgeValue: Double get() = when(this) {
    Die.DIE_GREEN -> 1.5
    Die.DIE_BLUE -> 1.5
    Die.DIE_BLACK -> 1.5
    else -> 2.0
  }

  /** C98 "Lift/Pass Comparison Factor": used when combining dice/icon EV with effect EV. */
  const val LIFT_PASS_COMPARISON_FACTOR = 0.1

  /**
   * "Look at the top 3 cards of your deck, put 1 on top, discard the others" combo value
   * (C17). Recognized from three consecutive `card_effect` entries:
   *   TOPDECK -> REVEALED_TOPDECK (count 3),
   *   REVEALED_TOPDECK -> DISCARD (count 2),
   *   REVEALED_TOPDECK -> TOPDECK (count 1).
   */
  const val LOOK_AT_TOP_3_KEEP_1 = CARD_FILTER_3

  /**
   * "Draw 2 cards, then put 2 cards on top of your deck" combo value (2 x C16). Recognized
   * from two consecutive `card_effect` entries:
   *   TOPDECK -> HAND (count 2),
   *   HAND -> TOPDECK (count 2).
   */
  const val DRAW_2_TOPDECK_2 = 2 * CARD_FILTER_2

  /**
   * Flat value for `filter_hand` ("discard any number of cards, then draw that many"). The
   * sheet's version wasn't scaled by hand size (C16 + C17), and per your direction this stays
   * flat rather than becoming hand-size-aware.
   */
  const val FILTER_HAND = CARD_FILTER_2 + CARD_FILTER_3

  /**
   * C4 "Effect EV": AVERAGE(Cards!S2:S1036), the average effect EV across every card in the
   * sheet. Used as the flat value for `reactivate_following` ("activate the effect of the
   * card below an additional time"), per your direction to hardcode this constant.
   */
  const val REACTIVATE_FOLLOWING = 2.200542645
}
