package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.random.Dice.averageValue
import com.redpup.justsendit.model.random.Dice.maxValue
import com.redpup.justsendit.model.skill.calculation.MatcherUtilities.coloredDieFrequency
import com.redpup.justsendit.model.skill.calculation.MatcherUtilities.dieColorOrWild
import com.redpup.justsendit.model.supply.proto.*
import com.redpup.justsendit.model.supply.proto.SkillCardEffectCondition.ConditionCase
import com.redpup.justsendit.model.supply.proto.SkillCardEffectCost.CostCase
import com.redpup.justsendit.util.TextProtoReaderWriterImpl
import com.redpup.justsendit.util.round

/** Like constants, but determined by the aggregated value of all cards. */
class ResolvedValues {
  private val parametersList =
    TextProtoReaderWriterImpl<SkillCardComputationValues, SkillCardComputationValuesList.Builder>(
      "src/main/resources/com/redpup/justsendit/model/shop/skill/skill_card_computation_values.textproto",
      SkillCardComputationValuesList::newBuilder,
      SkillCardComputationValuesList.Builder::getValuesList,
      SkillCardComputationValuesList.Builder::addAllValues,
    )

  private var totalCards = 0
  private var cardCosts = mapOf<Int, Int>()
  private var cardIcons = mapOf<Icon, Int>()

  private val parameters = parametersList().first().toBuilder()

  operator fun invoke(): SkillCardComputationValuesOrBuilder = parameters

  /** Updates the values in this based on [cards]. */
  fun update(cards: List<SkillCard>): SkillCardComputationValues {
    totalCards = cards.size
    cardCosts = cards.groupingBy { it.computed.suggestedCost }.eachCount()
    cardIcons = cards.flatMap { it.iconsList }.groupingBy { it }.eachCount()

    val computed = cards.map { it.computed }
    val current = parameters.build()
    with(parameters) {
      averageCost = computed.map { it.suggestedCost }.average()
      cardDraw = computed.map { it.totalExpectedValue }.average()
      effect = computed.map { it.effectExpectedValue }.average()
      icons = computed.map { it.iconExpectedValue }.average()
      cardFilter2 = 0.564 * cardDraw
      cardFilter3 = 0.846 * cardDraw
    }
    return parameters.build() - current
  }

  /** Writes the contents of [parameters] back to textproto file. */
  fun write() {
    parametersList.write(listOf(parameters.build()))
  }

  /** Cost to add to [SkillCardEffect] value. Will be 0 if none or negative if present. */
  val SkillCardEffectCost.effectCost: Double
    get() =
      when (costCase) {
        CostCase.DISCARD_CARD -> -(this@ResolvedValues().cardDraw * 0.8)
        CostCase.REMOVE_DIE -> -(removeDie?.dieColorOrWild()?.averageValue
          ?: (Die.DIE_BLUE.averageValue * Constants.WILD_DIE_PICK_FACTOR))

        CostCase.COST_NOT_SET -> 0.0
        null -> throw IllegalStateException()
      }

  /** Factor to apply to a [SkillCardEffect] value based on [conditionCase]. */
  fun SkillCardEffectCondition.effectConditionFactor(card: SkillCard): Double =
    when (conditionCase) {
      ConditionCase.CONDITION_NOT_SET -> 1.0 // No condition is always active.
      ConditionCase.SUCCESS -> 0.8
      ConditionCase.FAILURE -> 0.2
      ConditionCase.NEXT_CARD_COST ->
        cardCosts.entries.filter { it.key > card.computed.suggestedCost }
          .sumOf { it.value } / totalCards.toDouble()

      null -> throw IllegalStateException()
    }

  /** Factor to apply to a [SkillCardEffect] value based on [repeatCase]. */
  fun SkillCardEffectRepeat.effectRepeatFactor(card: SkillCard): Double = when (repeatCase) {
    SkillCardEffectRepeat.RepeatCase.REPEAT_NOT_SET -> 1.0

    SkillCardEffectRepeat.RepeatCase.SKILL_CARD_BELOW -> TODO()
    SkillCardEffectRepeat.RepeatCase.SKILL_CARD_ABOVE -> TODO()
    SkillCardEffectRepeat.RepeatCase.WOBBLE -> 1.0
    SkillCardEffectRepeat.RepeatCase.MATCHING_TAG_ON_CARDS_ABOVE -> card.iconsList.sumOf {
      cardIcons[it] ?: 0
    } / totalCards.toDouble()

    SkillCardEffectRepeat.RepeatCase.MATCHING_DIE -> matchingDie.coloredDieFrequency()
      ?: (Constants.WILD_DIE_PICK_FACTOR / Die.DIE_BLUE.maxValue)

    null -> throw IllegalStateException()
  }

  companion object {
    /**
     * "Look at the top 3 cards of your deck, put 1 on top, discard the others" combo value
     * (C17). Recognized from three consecutive `card_effect` entries:
     *   TOPDECK -> REVEALED_TOPDECK (count 3),
     *   REVEALED_TOPDECK -> DISCARD (count 2),
     *   REVEALED_TOPDECK -> TOPDECK (count 1).
     */
    val SkillCardComputationValuesOrBuilder.lookAtTop3Keep1: Double get() = cardFilter3

    /**
     * "Draw 2 cards, then put 2 cards on top of your deck" combo value (2 x C16). Recognized
     * from two consecutive `card_effect` entries:
     *   TOPDECK -> HAND (count 2),
     *   HAND -> TOPDECK (count 2).
     */
    val SkillCardComputationValuesOrBuilder.draw2Topdeck2: Double get() = 2 * cardFilter2

    /**
     * Flat value for `filter_hand` ("discard any number of cards, then draw that many"). The
     * sheet's version wasn't scaled by hand size (C16 + C17), and per your direction this stays
     * flat rather than becoming hand-size-aware.
     */
    val SkillCardComputationValuesOrBuilder.filterHand: Double get() = cardFilter2 + cardFilter3

    /** Returns the difference between this and [other] as a [SkillCardComputationValues]. */
    operator fun SkillCardComputationValues.minus(other: SkillCardComputationValues): SkillCardComputationValues {
      val self = this
      return skillCardComputationValues {
        this.averageCost = (self.averageCost - other.averageCost).round(6)
        this.cardDraw = (self.cardDraw - other.cardDraw).round(6)
        this.effect = (self.effect - other.effect).round(6)
        this.icons = (self.icons - other.icons).round(6)
        this.cardFilter2 = (self.cardFilter2 - other.cardFilter2).round(6)
        this.cardFilter3 = (self.cardFilter3 - other.cardFilter3).round(6)
      }
    }

    /** Returns true if all values in this are 0. */
    fun SkillCardComputationValues.isZero() =
      averageCost == 0.0 &&
        cardDraw == 0.0 &&
        effect == 0.0 &&
        icons == 0.0 &&
        cardFilter2 == 0.0 &&
        cardFilter3 == 0.0
  }
}