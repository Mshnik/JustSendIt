package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.random.Dice.averageValue
import com.redpup.justsendit.model.skill.calculation.Constants.LIFT_PASS_COMPARISON_FACTOR
import com.redpup.justsendit.model.skill.calculation.Constants.MAX_UPGRADE_COST
import com.redpup.justsendit.model.skill.calculation.Constants.MIN_UPGRADE_COST
import com.redpup.justsendit.model.skill.calculation.Constants.NUDGE_VALUE
import com.redpup.justsendit.model.skill.calculation.Constants.REROLL_VALUE
import com.redpup.justsendit.model.skill.calculation.Constants.TIMING_FACTOR
import com.redpup.justsendit.model.skill.calculation.Constants.WILD_DIE_PICK_FACTOR
import com.redpup.justsendit.model.skill.calculation.MatcherUtilities.dieColorOrWild
import com.redpup.justsendit.model.skill.calculation.ResolvedValues.Companion.filterHand
import com.redpup.justsendit.model.skill.calculation.ResolvedValues.Companion.isZero
import com.redpup.justsendit.model.supply.proto.*
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.util.TextProtoReaderImpl
import com.redpup.justsendit.util.TextProtoReaderWriterImpl
import java.io.File

/** TODO: Description. */
fun main() {
  val textproto = "src/main/resources/com/redpup/justsendit/model/shop/skill/skill_cards.textproto"
  val csv = "src/main/resources/com/redpup/justsendit/model/shop/skill/skill_cards_computed.csv"
  SkillCalculator(textproto).updateComputedFields()
  SkillCsvWriter(textproto, csv).write()
}

/** Computation class that resolves [SkillCard.Computed] for all skill cards in [path]. */
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
    resolvedValues.update(cards)
    println("Found ${cards.size} cards")
    for (iteration in 0 until resolutionIterations) {
      println("  Iteration $iteration")
      cards = cards.map { it.copy { computed = it.compute() } }.let { computeCosts(it) }
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
  private fun SkillCard.compute(): SkillCard.Computed {
    val diceExpectedValue = diceExpectedValue()
    val iconExpectedValue = iconExpectedValue()
    val effectExpectedValue = effectExpectedValue()
    val totalExpectedValue =
      totalExpectedValue(diceExpectedValue, iconExpectedValue, effectExpectedValue)

    return computed {
      this.diceExpectedValue = diceExpectedValue
      this.iconExpectedValue = iconExpectedValue
      this.effectExpectedValue = effectExpectedValue
      this.totalExpectedValue = totalExpectedValue
    }
  }

  /**
   * Equivalent to the sheet's DGET-against-a-lookup-table approach, but computed directly:
   * for n dice of a given color, expected roll sums (sides+1)/2 per die.
   */
  private fun SkillCard.diceExpectedValue(): Double = diceList.sumOf { it.averageValue }

  /** Sum of values of icons on [this]. */
  private fun SkillCard.iconExpectedValue(): Double =
    with(Icons) {
      iconsList.sumOf { it.frequency }
    }

  /** Total value of [this] effects. 0 if there are no effects. */
  private fun SkillCard.effectExpectedValue(): Double {
    if (effectsList.isEmpty()) {
      return 0.0
    }

    val groups = skillCalculationCardEffects.findCardEffectGroups(effectsList)
    val consumedIndices = groups.flatMap { it.consumedIndices }.toSet()
    val groupTotal = groups.sumOf { it.value }

    val perEntryTotal =
      effectsList.withIndex().filter { (index, _) -> index !in consumedIndices }
        .sumOf { (_, effect) ->
          singleEffectValue(
            this,
            effectCondition,
            effectCost,
            effect,
            effectRepeat
          )
        }

    return TIMING_FACTOR * (perEntryTotal + groupTotal)
  }

  /** Total expected value of [card]. Note that this is not just the sum. */
  private fun SkillCard.totalExpectedValue(
    diceExpectedValue: Double,
    iconExpectedValue: Double,
    effectExpectedValue: Double,
  ): Double {
    val nonTextEv = diceExpectedValue + iconExpectedValue

    return when (category) {
      EffectCategory.EFFECT_CATEGORY_LIFT -> maxOf(
        nonTextEv,
        effectExpectedValue
      ) + (nonTextEv + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR

      EffectCategory.EFFECT_CATEGORY_REST -> maxOf(
        diceExpectedValue,
        effectExpectedValue
      ) + (diceExpectedValue + effectExpectedValue) * LIFT_PASS_COMPARISON_FACTOR +
        iconExpectedValue

      EffectCategory.EFFECT_CATEGORY_PLAY,
      EffectCategory.EFFECT_CATEGORY_RIDE,
      EffectCategory.EFFECT_CATEGORY_FINALE,
      EffectCategory.EFFECT_CATEGORY_UNSET,
      -> effectExpectedValue + nonTextEv

      EffectCategory.UNRECOGNIZED, null -> throw IllegalStateException()
    }
  }

  /** Computes the net value of a single [condition] and [effect]. */
  private fun singleEffectValue(
    card: SkillCard,
    condition: SkillCardEffectCondition,
    cost: SkillCardEffectCost,
    effect: SkillCardEffect,
    repeat: SkillCardEffectRepeat,
  ): Double =
    with(resolvedValues) {
      (effect.baseEffectValue(card) *
        condition.effectConditionFactor(card) *
        repeat.effectRepeatFactor(card)) +
        cost.effectCost
    }

  /** Computes the value of the effect alone (ignoring repeat factor and cost). */
  private fun SkillCardEffect.baseEffectValue(card: SkillCard): Double = when (effectCase) {
    SkillCardEffect.EffectCase.ALTER_DIE -> alterDieValue(alterDie)
    SkillCardEffect.EffectCase.GAIN -> gain.gainValue()
    SkillCardEffect.EffectCase.IGNORE_WOBBLE -> Constants.PREVENT_WOBBLE
    SkillCardEffect.EffectCase.REACTIVATE_FOLLOWING -> resolvedValues().effect
    SkillCardEffect.EffectCase.FILTER_HAND -> resolvedValues().filterHand
    SkillCardEffect.EffectCase.REPLENISH_SHOP -> Constants.REFRESH_SHOP
    SkillCardEffect.EffectCase.EXTRA_TURN -> Constants.ADDITIONAL_TURN
    SkillCardEffect.EffectCase.CARD_EFFECT -> cardEffect.singleCardEffectValue()
    SkillCardEffect.EffectCase.GAIN_OWN_TAGS -> card.iconExpectedValue()
    SkillCardEffect.EffectCase.GAIN_TAGS_BELOW -> resolvedValues().icons
    SkillCardEffect.EffectCase.DRAW_FROM_PLAY -> resolvedValues().cardDraw
    SkillCardEffect.EffectCase.MOVE_TILE -> Constants.MOVE_TILE
    SkillCardEffect.EffectCase.GAIN_FUN_EQUAL_TO_NEXT_CARD_COST -> resolvedValues().averageCost * Constants.POINTS
    SkillCardEffect.EffectCase.GAIN_FUN_EQUAL_TO_VALUE_ROLLED ->
      card.effectCost.removeDie.dieColorOrWild()?.averageValue
        ?: (Die.DIE_BLUE.averageValue * WILD_DIE_PICK_FACTOR)

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
  private fun GainEffect.gainValue(): Double = when (gainCase) {
    GainEffect.GainCase.SKILL -> skill.toDouble()
    GainEffect.GainCase.POINTS -> points.toDouble() * Constants.POINTS
    GainEffect.GainCase.BUYS -> buys.toDouble() * Constants.BUY
    GainEffect.GainCase.TRASHES -> trashes.toDouble() * resolvedValues().trashCard
    GainEffect.GainCase.DIE -> die.averageValue
    GainEffect.GainCase.GAIN_NOT_SET, null -> throw IllegalStateException()
  }

  private fun CardEffect.singleCardEffectValue(): Double =
    if (sourceZone == SkillCardZone.SKILL_CARD_ZONE_TOPDECK && destinationZone == SkillCardZone.SKILL_CARD_ZONE_HAND) {
      resolvedValues().cardDraw * count
    } else {
      throw IllegalArgumentException()
    }

  /** Updates the costs in all [SkillCard.Computed] sections on all [SkillCard]s. */
  private fun computeCosts(cards: List<SkillCard>): List<SkillCard> {
    val minValue = cards
      .filter { it.type == SkillCardType.SKILL_CARD_TYPE_UPGRADE }
      .minOf { it.computed.totalExpectedValue }
    val maxValue = cards.maxOf { it.computed.totalExpectedValue }

    return cards.map { card ->
      card.copy {
        computed = card.computed.copy {
          suggestedCost = computeCost(card, minValue, maxValue)
        }
      }
    }
  }

  /** Computes the suggested cost given the inputs. */
  private fun computeCost(
    card: SkillCard,
    minValue: Double,
    maxValue: Double,
  ): Int {
    if (card.type == SkillCardType.SKILL_CARD_TYPE_STARTER) {
      return 0
    }
    val percentile = (card.computed.totalExpectedValue - minValue) / (maxValue - minValue)
    return (percentile * (MAX_UPGRADE_COST - MIN_UPGRADE_COST)).toInt() + MIN_UPGRADE_COST
  }
}

/** Output writer for writing computed skill content back to Csv. */
class SkillCsvWriter(private val input: String, private val output: String) {
  private val reader = TextProtoReaderImpl(
    input,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList
  )

  fun write() {
    val cards = reader()
    File(output).writer().use { writer ->
      writer.write("Name,Cost,ExpectedValue\n")
      cards.map { "${it.name},${it.computed.suggestedCost},${it.computed.totalExpectedValue}" }
        .forEach {
          writer.write(it)
          writer.write("\n")
        }
    }
  }
}