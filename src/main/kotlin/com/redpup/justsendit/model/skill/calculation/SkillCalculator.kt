package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.random.Dice.maxValue
import com.redpup.justsendit.model.skill.calculation.Constants.EFFECT_FACTOR
import com.redpup.justsendit.model.skill.calculation.Constants.EFFECT_REPEAT_FACTOR
import com.redpup.justsendit.model.skill.calculation.Constants.LIFT_PASS_COMPARISON_FACTOR
import com.redpup.justsendit.model.skill.calculation.Constants.NUDGE_VALUE
import com.redpup.justsendit.model.skill.calculation.Constants.REROLL_VALUE
import com.redpup.justsendit.model.skill.calculation.Constants.TIMING_FACTOR
import com.redpup.justsendit.model.skill.calculation.MatcherUtilities.dieColorOrWild
import com.redpup.justsendit.model.skill.calculation.ResolvedValues.Companion.filterHand
import com.redpup.justsendit.model.skill.calculation.ResolvedValues.Companion.isZero
import com.redpup.justsendit.model.supply.proto.*
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.util.TextProtoReaderWriterImpl

/** TODO: Description. */
fun main() {
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/skill_cards.textproto").updateComputedFields()
}

/** TODO: Description */
class SkillCalculator(private val path: String, private val resolutionIterations: Int = 20) {
  private val readerWriter = TextProtoReaderWriterImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    SkillCardList.Builder::addAllCards,
  )
  private val resolvedValues = ResolvedValues()
  private val skillCalculationCardEffects = SkillCalculationCardEffects(resolvedValues)

  /** TODO: Description. */
  fun updateComputedFields() {
    println("Processing: $path")
    var cards = readerWriter()
    for (iteration in 0 until resolutionIterations) {
      println("  Iteration $iteration")
      cards = cards.map { it.copy { computed = compute(it) } }
      val delta = resolvedValues.update(cards)
      println("  Delta: ${delta.toString().replace("\n", " ")}")
      if (delta.isZero()) {
        break
      }
    }
    readerWriter.write(cards)
    resolvedValues.write()
    println("Done.")
  }

  /**
   * Recomputes [SkillCard.Computed] for [card], mirroring the "Text EV" (S), "Dice EV" (W),
   * "Bonus EV" (X), and "Total EV" (Z) formulas from the Cards tab of Redesign.xlsx.
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
    repeat(card.greenDice) { add(Die.DIE_GREEN) }
    repeat(card.blueDice) { add(Die.DIE_BLUE) }
    repeat(card.blackDice) { add(Die.DIE_BLACK) }
  }.sumOf { (it.maxValue + 1.0) / 2.0 }

  /** Sum of values of icons on [card]. */
  private fun iconExpectedValue(card: SkillCard): Double =
    with(Icons) {
      card.iconsList.sumOf { it.frequency }
    }

  /** Total value of [card] effects. 0 if there are no effects. */
  private fun effectExpectedValue(card: SkillCard): Double {
    if (card.effectsList.isEmpty()) {
      return 0.0
    }

    val groups = skillCalculationCardEffects.findCardEffectGroups(card.effectsList)
    val consumedIndices = groups.flatMap { it.consumedIndices }.toSet()
    val groupTotal = groups.sumOf { it.value }

    val perEntryTotal =
      card.effectsList.withIndex().filter { (index, _) -> index !in consumedIndices }
        .sumOf { (_, effect) -> singleEffectValue(card.effectCondition, card.effectCost, effect) }

    return card.TIMING_FACTOR * (perEntryTotal + groupTotal)
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
      EffectCategory.EFFECT_CATEGORY_LIFT -> maxOf(
        nonTextEv,
        effectExpectedValue
      ) + (nonTextEv + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR

      EffectCategory.EFFECT_CATEGORY_PASS -> maxOf(
        diceExpectedValue,
        effectExpectedValue
      ) + (diceExpectedValue + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR +
        iconExpectedValue

      EffectCategory.EFFECT_CATEGORY_PLAY,
      EffectCategory.EFFECT_CATEGORY_FIRST,
      EffectCategory.EFFECT_CATEGORY_LAST,
      EffectCategory.EFFECT_CATEGORY_UNSET,
      -> effectExpectedValue + nonTextEv

      EffectCategory.UNRECOGNIZED, null -> throw IllegalStateException()
    }
  }

  /** Computes the net value of a single [condition] and [effect]. */
  private fun singleEffectValue(
    condition: SkillCardEffectCondition,
    cost: SkillCardEffectCost,
    effect: SkillCardEffect,
  ): Double =
    with(resolvedValues) {
      effect.baseEffectValue() * condition.EFFECT_FACTOR + cost.effectCost
    }

  /** Computes the value of the effect alone (ignoring factor and cost). */
  private fun SkillCardEffect.baseEffectValue(): Double = when (effectCase) {
    SkillCardEffect.EffectCase.ALTER_DIE -> alterDieValue(alterDie)
    SkillCardEffect.EffectCase.GAIN -> gainValue(gain)
    SkillCardEffect.EffectCase.IGNORE_WOBBLE -> Constants.PREVENT_WOBBLE
    SkillCardEffect.EffectCase.REACTIVATE_FOLLOWING -> resolvedValues().reactivate
    SkillCardEffect.EffectCase.FILTER_HAND -> resolvedValues().filterHand
    SkillCardEffect.EffectCase.REPLENISH_SHOP -> Constants.REFRESH_SHOP
    SkillCardEffect.EffectCase.EXTRA_TURN -> Constants.ADDITIONAL_TURN
    SkillCardEffect.EffectCase.CARD_EFFECT -> singleCardEffectValue(cardEffect)
    SkillCardEffect.EffectCase.EFFECT_NOT_SET, null -> throw IllegalStateException()
  }

  /** Returns the computed value of the given [AlterDieEffect]. */
  private fun alterDieValue(alterDie: AlterDieEffect): Double {
    val color = alterDie.dieMatcher.dieColorOrWild()
    return when (alterDie.effectCase) {
      AlterDieEffect.EffectCase.REROLL -> color.REROLL_VALUE
      AlterDieEffect.EffectCase.NUDGE -> color.NUDGE_VALUE
      AlterDieEffect.EffectCase.EFFECT_NOT_SET, null -> throw IllegalStateException()
    }
  }

  /** Value of the given [gain] effect. */
  private fun gainValue(gain: GainEffect): Double = when (gain.gainCase) {
    GainEffect.GainCase.SKILL -> gain.skill.toDouble()
    GainEffect.GainCase.POINTS -> gain.points.toDouble() * Constants.POINTS
    GainEffect.GainCase.BUYS -> gain.buys.toDouble() * Constants.BUY
    GainEffect.GainCase.TRASHES -> gain.trashes.toDouble() * resolvedValues().trashCard
    GainEffect.GainCase.GAIN_NOT_SET, null -> throw IllegalStateException()
  } * gain.EFFECT_REPEAT_FACTOR


  private fun singleCardEffectValue(cardEffect: CardEffect): Double =
    if (cardEffect.sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK && cardEffect.destinationZone == SkillCardZone.SKILL_CARD_ZONE_HAND) {
      resolvedValues().cardDraw * cardEffect.count
    } else {
      throw IllegalArgumentException()
    }
}
