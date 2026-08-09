package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.random.Dice.maxValue
import com.redpup.justsendit.model.skill.calculation.SkillCalculationUtilities.dieColorOrWild
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.EFFECT_COST
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.EFFECT_FACTOR
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.EFFECT_REPEAT_FACTOR
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.EV
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.LIFT_PASS_COMPARISON_FACTOR
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.NUDGE_VALUE
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.REROLL_VALUE
import com.redpup.justsendit.model.skill.calculation.SkillCardEvConstants.TIMING_FACTOR
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.draw2Topdeck2
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.filterHand
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.isZero
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.lookAtTop3Keep1
import com.redpup.justsendit.model.supply.proto.*
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.util.TextProtoReaderWriterImpl

/** TODO: Description. */
fun main() {
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/skill_cards.textproto")
    .updateComputedFields()
}

/** TODO: Description */
class SkillCalculator(private val path: String, private val resolutionIterations: Int = 20) {
  private val readerWriter = TextProtoReaderWriterImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    SkillCardList.Builder::addAllCards,
  )
  private val resolvedValues = SkillCardEvResolvedValues()

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
   *
   * `suggested_cost` is intentionally left unset — it depends on the min/max EV across every
   * Shop card, not just this one, and per your direction will be computed by a separate
   * fleet-wide method.
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
    baseEffectValue(effect) * effect.EFFECT_FACTOR + effect.EFFECT_COST

  private fun baseEffectValue(effect: SkillCardEffect): Double = when (effect.effectCase) {
    SkillCardEffect.EffectCase.ALTER_DIE -> alterDieValue(effect.alterDie)
    SkillCardEffect.EffectCase.GAIN -> gainValue(effect.gain)
    SkillCardEffect.EffectCase.IGNORE_WOBBLE -> SkillCardEvConstants.PREVENT_WOBBLE
    SkillCardEffect.EffectCase.REACTIVATE_FOLLOWING -> resolvedValues().reactivate
    SkillCardEffect.EffectCase.FILTER_HAND -> resolvedValues().filterHand
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
    val color = alterDie.dieMatcher.dieColorOrWild()
    return when (alterDie.effectCase) {
      AlterDieEffect.EffectCase.REROLL -> color.REROLL_VALUE
      AlterDieEffect.EffectCase.NUDGE -> color.NUDGE_VALUE
      else -> 0.0
    }
  }

  /** Value of the given [gain] effect. */
  private fun gainValue(gain: GainEffect): Double = when (gain.gainCase) {
    GainEffect.GainCase.SKILL -> gain.skill.toDouble()
    GainEffect.GainCase.POINTS -> gain.points.toDouble() * SkillCardEvConstants.POINTS
    GainEffect.GainCase.BUYS -> gain.buys.toDouble() * SkillCardEvConstants.BUY
    GainEffect.GainCase.TRASHES -> gain.trashes.toDouble() * resolvedValues().trashCard
    else -> 0.0
  } * gain.EFFECT_REPEAT_FACTOR


  private fun singleCardEffectValue(cardEffect: CardEffect): Double =
    if (cardEffect.sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK &&
      cardEffect.destinationZone == SkillCardZone.SKILL_CARD_ZONE_HAND
    ) {
      resolvedValues().cardDraw * cardEffect.count
    } else {
      0.0
    }

  private class CardEffectGroupMatch(val value: Double, val consumedIndices: Set<Int>)


  /**
   * Scans [effects] for the two known multi-step `card_effect` combos (see the doc comments
   * on [SkillCardEvResolvedValues.lookAtTop3Keep1] and [SkillCardEvResolvedValues.draw2Topdeck2]),
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
            resolvedValues().lookAtTop3Keep1, setOf(i, i + 1, i + 2)
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
        groups.add(CardEffectGroupMatch(resolvedValues().draw2Topdeck2, setOf(i, i + 1)))
        i += 2
        continue
      }

      i++
    }
    return groups
  }
}
