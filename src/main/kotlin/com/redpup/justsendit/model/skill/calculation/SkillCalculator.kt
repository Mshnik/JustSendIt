package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.EV
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.LIFT_PASS_COMPARISON_FACTOR
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.cost
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.factor
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.nudgeValue
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.repeatFactor
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.rerollValue
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.timingFactor
import com.redpup.justsendit.model.supply.proto.*
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.util.TextProtoReaderWriterImpl
import com.redpup.matchers.proto.Matcher

/** TODO: Description. */
fun main() {
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Shop.textproto")
    .updateComputedFields()
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Starter.textproto")
    .updateComputedFields()
}

/** TODO: Description */
class SkillCalculator(private val path: String) {
  private val readerWriter = TextProtoReaderWriterImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    SkillCardList.Builder::addAllCards,
  )

  /** TODO: Description. */
  fun updateComputedFields() {
    readerWriter.update { it.copy { computed = compute(it) } }
  }

  /**
   * Recomputes [SkillCard.Computed] for [card], mirroring the "Text EV" (S), "Dice EV" (W),
   * "Bonus EV" (X), and "Total EV" (Z) formulas from the Cards tab of Redesign.xlsx.
   *
   * `suggested_cost` is intentionally left unset — it depends on the min/max EV across every
   * Shop card, not just this one, and per your direction will be computed by a separate
   * fleet-wide method.
   *
   * Known open issues / assumptions, see inline TODOs:
   *  - The `matchers.Matcher` shape used by [dieColorOrWild] is inferred from example instances,
   *    not from matcher.proto itself (not available at the time this was written). Verify the
   *    accessor names against the real generated class.
   *  - `GainEffect` is assumed to have a `fun` field (oneof `gain`) alongside `skill`/`points`/
   *    `buys`/`trashes`, matching your sample data — this isn't present in the skill.proto
   *    snippet you originally shared, so double check the proto has it.
   *  - `card_effect` combos beyond the three patterns seen in your sample data (draw 1; draw 2 +
   *    topdeck 2; look at top 3 + keep 1 + discard 2) aren't recognized and contribute 0 EV.
   */
  private fun compute(card: SkillCard): SkillCard.Computed {
    val diceExpectedValue = diceExpectedValue(card)
    val iconExpectedValue = iconExpectedValue(card)
    val effectExpectedValue = effectExpectedValue(card)
    val totalExpectedValue = totalExpectedValue(
      card, diceExpectedValue, iconExpectedValue, effectExpectedValue,
    )

    return computed {
      this.diceExpectedValue = diceExpectedValue
      this.iconExpectedValue = iconExpectedValue
      this.effectExpectedValue = effectExpectedValue
      this.totalExpectedValue = totalExpectedValue
    }
  }

  /**
   * Equivalent to the sheet's DGET-against-a-lookup-table approach, but computed directly:
   * for n dice of a given color, wobble chance sums 1/sides per die, expected roll sums
   * (sides+1)/2 per die, and the combined value is expectedValue * (3 - expectedWobbles) / 3.
   */
  private fun diceExpectedValue(card: SkillCard): Double = buildList {
    repeat(card.greenDice) { add(SkillCardEvConstants.GREEN_DIE_SIDES) }
    repeat(card.blueDice) { add(SkillCardEvConstants.BLUE_DIE_SIDES) }
    repeat(card.blackDice) { add(SkillCardEvConstants.BLACK_DIE_SIDES) }
  }.sumOf { (it + 1.0) / 2.0 }

  /** Sum of values of icons on [card]. */
  private fun iconExpectedValue(card: SkillCard): Double =
    card.iconsList.sumOf { it.EV }

  /** Total value of [card] effects. 0 if there are no effects. */
  private fun effectExpectedValue(card: SkillCard): Double {
    if (card.effectsList.isEmpty()) return 0.0

    val groups = findCardEffectGroups(card.effectsList)
    val consumedIndices = groups.flatMap { it.consumedIndices }.toSet()
    val groupTotal = groups.sumOf { it.value }

    val perEntryTotal = card.effectsList
      .withIndex()
      .filter { (index, _) -> index !in consumedIndices }
      .sumOf { (_, effect) -> singleEffectValue(effect) }

    return card.timingFactor * (perEntryTotal + groupTotal)
  }

  /** Total expected value of [card]. Note that this is not just the sum. */
  private fun totalExpectedValue(
    card: SkillCard,
    diceExpectedValue: Double,
    iconExpectedValue: Double,
    effectExpectedValue: Double,
  ): Double {
    val nonTextEv = diceExpectedValue + iconExpectedValue

    return when (card.category) {
      EffectCategory.EFFECT_CATEGORY_LIFT ->
        maxOf(nonTextEv, effectExpectedValue) +
          (nonTextEv + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR

      EffectCategory.EFFECT_CATEGORY_PASS ->
        maxOf(diceExpectedValue, effectExpectedValue) +
          (diceExpectedValue + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR +
          iconExpectedValue

      else -> effectExpectedValue + nonTextEv
    }
  }

  /** Computes the value of a single [effect]. */
  private fun singleEffectValue(effect: SkillCardEffect): Double =
    baseEffectValue(effect) * effect.factor + effect.cost

  private fun baseEffectValue(effect: SkillCardEffect): Double = when (effect.effectCase) {
    SkillCardEffect.EffectCase.ALTER_DIE -> alterDieValue(effect.alterDie)
    SkillCardEffect.EffectCase.GAIN -> gainValue(effect.gain)
    SkillCardEffect.EffectCase.IGNORE_WOBBLE -> SkillCardEvConstants.PREVENT_WOBBLE
    SkillCardEffect.EffectCase.REACTIVATE_FOLLOWING -> SkillCardEvConstants.REACTIVATE_FOLLOWING
    SkillCardEffect.EffectCase.FILTER_HAND -> SkillCardEvConstants.FILTER_HAND
    SkillCardEffect.EffectCase.REPLENISH_SHOP -> SkillCardEvConstants.REFRESH_SHOP
    SkillCardEffect.EffectCase.EXTRA_TURN -> SkillCardEvConstants.ADDITIONAL_TURN
    // Only reached for a `card_effect` entry that wasn't consumed by findCardEffectGroups,
    // i.e. a single un-grouped move. Only "draw a card" (topdeck -> hand, count 1) is a
    // known single-entry pattern; anything else is unrecognized and contributes 0.
    SkillCardEffect.EffectCase.CARD_EFFECT -> singleCardEffectValue(effect.cardEffect)
    else -> 0.0
  }

  /** Returns the computed value of the given [AlterDieEffect]. */
  private fun alterDieValue(alterDie: AlterDieEffect): Double {
    val color = dieColorOrWild(alterDie.dieMatcher)
    return when (alterDie.effectCase) {
      AlterDieEffect.EffectCase.REROLL -> color.rerollValue
      AlterDieEffect.EffectCase.NUDGE -> color.nudgeValue
      else -> 0.0
    }
  }

  /** Value of the given [gain] effect. */
  private fun gainValue(gain: GainEffect): Double = when (gain.gainCase) {
    GainEffect.GainCase.SKILL -> gain.skill.toDouble()
    GainEffect.GainCase.POINTS -> gain.points.toDouble()
    GainEffect.GainCase.BUYS -> gain.buys.toDouble() * SkillCardEvConstants.BUY
    GainEffect.GainCase.TRASHES -> gain.trashes.toDouble() * SkillCardEvConstants.TRASH_CARD_DECK_DISCARD
    else -> 0.0
  } * gain.repeatFactor

  /**
   * Returns which die color a [matcher] targets, or `null` for "wild"/any (a `constant_matcher
   * = true` matcher, e.g. "Reroll Wild" / "Slide Wild").
   */
  private fun dieColorOrWild(matcher: Matcher): Die? {
    if (matcher.hasConstantMatcher() && matcher.constantMatcher) {
      return null
    } else if (!matcher.hasEnumMatcher()) {
      return null
    }

    val enumMatcher = matcher.enumMatcher
    if (enumMatcher.enumTypeName != Die.getDescriptor().fullName) {
      return null
    }

    if (enumMatcher.nameMatcher.hasStringMatcher()) {
      return runCatching {
        Die.valueOf(enumMatcher.nameMatcher.stringMatcher.value)
      }.getOrNull()
    } else if (enumMatcher.numberMatcher.hasValueMatcher()) {
      return runCatching {
        Die.entries[enumMatcher.numberMatcher.valueMatcher.int32Value]
      }.getOrNull()
    }

    return null
  }
}

private fun singleCardEffectValue(cardEffect: CardEffect): Double =
  if (cardEffect.sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK &&
    cardEffect.destinationZone == SkillCardZone.SKILL_CARD_ZONE_HAND &&
    cardEffect.count == 1
  ) {
    SkillCardEvConstants.CARD_DRAW
  } else {
    0.0
  }

// ---- Multi-entry `card_effect` combo recognition ----

private class CardEffectGroupMatch(val value: Double, val consumedIndices: Set<Int>)

/**
 * Scans [effects] for the two known multi-step `card_effect` combos (see the doc comments
 * on [SkillCardEvConstants.LOOK_AT_TOP_3_KEEP_1] and [SkillCardEvConstants.DRAW_2_TOPDECK_2]),
 * each spread across consecutive, condition-free `card_effect` entries in the list.
 */
private fun findCardEffectGroups(effects: List<SkillCardEffect>): List<CardEffectGroupMatch> {
  fun cardEffectAt(index: Int): CardEffect? {
    val entry = effects.getOrNull(index) ?: return null
    if (entry.effectCase != SkillCardEffect.EffectCase.CARD_EFFECT) return null
    if (entry.conditionCase != SkillCardEffect.ConditionCase.CONDITION_NOT_SET) return null
    return entry.cardEffect
  }

  val groups = mutableListOf<CardEffectGroupMatch>()
  var i = 0
  while (i < effects.size) {
    val a = cardEffectAt(i)
    if (a == null) {
      i++
      continue
    }

    val b = cardEffectAt(i + 1)
    val c = cardEffectAt(i + 2)
    if (b != null && c != null &&
      a.sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK &&
      a.destinationZone == SkillCardZone.SKILL_CARD_ZONE_REVEALED_TOPDECK && a.count == 3 &&
      b.sourceZone == SkillCardZone.SKILL_CARD_ZONE_REVEALED_TOPDECK &&
      b.destinationZone == SkillCardZone.SKILL_CARD_ZONE_DISCARD && b.count == 2 &&
      c.sourceZone == SkillCardZone.SKILL_CARD_ZONE_REVEALED_TOPDECK &&
      c.destinationZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK && c.count == 1
    ) {
      groups.add(
        CardEffectGroupMatch(
          SkillCardEvConstants.LOOK_AT_TOP_3_KEEP_1,
          setOf(i, i + 1, i + 2)
        )
      )
      i += 3
      continue
    }

    if (b != null &&
      a.sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK &&
      a.destinationZone == SkillCardZone.SKILL_CARD_ZONE_HAND && a.count == 2 &&
      b.sourceZone == SkillCardZone.SKILL_CARD_ZONE_HAND &&
      b.destinationZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK && b.count == 2
    ) {
      groups.add(CardEffectGroupMatch(SkillCardEvConstants.DRAW_2_TOPDECK_2, setOf(i, i + 1)))
      i += 2
      continue
    }

    i++
  }
  return groups
}

// ---- Die-matcher color resolution ----
