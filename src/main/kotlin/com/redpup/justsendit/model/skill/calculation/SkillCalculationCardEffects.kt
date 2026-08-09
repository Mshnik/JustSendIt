package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.proto.SkillCardZone.*
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.draw2Topdeck2
import com.redpup.justsendit.model.skill.calculation.SkillCardEvResolvedValues.Companion.lookAtTop3Keep1
import com.redpup.justsendit.model.supply.proto.CardEffect
import com.redpup.justsendit.model.supply.proto.SkillCardEffect

class CardEffectGroupMatch(val value: Double, val consumedIndices: Set<Int>)

/** Utilities for operating on [CardEffect]s in skill calculations.*/
class SkillCalculationCardEffects(private val resolvedValues: SkillCardEvResolvedValues) {

  /**
   * Scans [effects] for the two known multi-step `card_effect` combos (see the doc comments
   * on [SkillCardEvResolvedValues.lookAtTop3Keep1] and [SkillCardEvResolvedValues.draw2Topdeck2]),
   * each spread across consecutive, condition-free `card_effect` entries in the list.
   */
  fun findCardEffectGroups(effects: List<SkillCardEffect>): List<CardEffectGroupMatch> {
    val groups = mutableListOf<CardEffectGroupMatch>()
    var i = 0
    while (i < effects.size) {
      val a = effects.cardEffectAt(i)
      if (a == null) {
        i++
        continue
      }

      val b = effects.cardEffectAt(i + 1)
      val c = effects.cardEffectAt(i + 2)
      val tripleMatch = Triple(a, b, c).findMatchingEffect()
      val pairMatch = Pair(a, b).findMatchingEffect()

      if (tripleMatch != null) {
        groups.add(CardEffectGroupMatch(tripleMatch, setOf(i, i + 1, i + 2)))
        i += 3
      } else if (pairMatch != null) {
        groups.add(CardEffectGroupMatch(pairMatch, setOf(i, i + 1)))
        i += 2
      } else {
        i++
      }
    }
    return groups
  }

  /** Returns the [CardEffect] at the given index, or null if it is not a [CardEffect]. */
  private fun List<SkillCardEffect>.cardEffectAt(index: Int): CardEffect? = getOrNull(index)
    ?.takeIf { it.effectCase == SkillCardEffect.EffectCase.CARD_EFFECT }
    ?.takeIf { it.conditionCase == SkillCardEffect.ConditionCase.CONDITION_NOT_SET }
    ?.cardEffect

  /** Returns true iff [CardEffect] matches the given parameters. */
  private fun CardEffect?.matches(
    source: SkillCardZone,
    destination: SkillCardZone,
    count: Int,
  ): Boolean =
    this != null
      && this.sourceZone == source
      && this.destinationZone == destination
      && this.count == count

  /**
   * Returns a value from [SkillCardEvResolvedValues] matching the given three effects,
   * or null if none.
   */
  private fun Triple<CardEffect?, CardEffect?, CardEffect?>.findMatchingEffect(): Double? {
    if (first == null || second == null || third == null) {
      return null
    } else if (
      first.matches(SKILL_CARD_ZONE_TOPDECK, SKILL_CARD_ZONE_REVEALED_TOPDECK, 3)
      && second.matches(SKILL_CARD_ZONE_REVEALED_TOPDECK, SKILL_CARD_ZONE_DISCARD, 2)
      && third.matches(SKILL_CARD_ZONE_REVEALED_TOPDECK, SKILL_CARD_ZONE_TOPDECK, 1)
    ) {
      return resolvedValues().lookAtTop3Keep1
    }

    return null
  }

  /**
   * Returns a value from [SkillCardEvResolvedValues] matching the given two effects,
   * or null if none.
   */
  private fun Pair<CardEffect?, CardEffect?>.findMatchingEffect(): Double? {
    if (first == null || second == null) {
      return null
    } else if (
      first.matches(SKILL_CARD_ZONE_TOPDECK, SKILL_CARD_ZONE_HAND, 2)
      && second.matches(SKILL_CARD_ZONE_HAND, SKILL_CARD_ZONE_TOPDECK, 2)
    ) {
      return resolvedValues().draw2Topdeck2
    }

    return null
  }
}