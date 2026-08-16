package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.player.proto.DieRoll
import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.random.Dice.maxValue
import com.redpup.matchers.proto.Matcher

/** Extra [Matcher] utilities for calculating skill card values. */
internal object MatcherUtilities {

  /** Returns the frequency [0,1] of a matcher matching a roll of a die. Returns null for wild dice. */
  fun Matcher.coloredDieFrequency(): Double? {
    check(hasMessageMatcher())
    check(messageMatcher.messageTypeName == DieRoll.getDescriptor().fullName)

    return messageMatcher.fieldsList.firstOrNull { it.fieldName == "die" || it.fieldNumber == DieRoll.DIE_FIELD_NUMBER }
      ?.matcher
      ?.dieColorOrWild()
      ?.let { 1.0 / it.maxValue }
  }

  /**
   * Returns which die color a this targets, or `null` for "wild"/any (a `constant_matcher
   * = true` matcher, e.g. "Reroll Wild" / "Slide Wild").
   */
  fun Matcher.dieColorOrWild(): Die? {
    if (hasConstantMatcher()) {
      check(constantMatcher)
      return null
    }

    val enumMatcher = enumMatcher
    check(enumMatcher.enumTypeName == Die.getDescriptor().fullName)

    if (enumMatcher.nameMatcher.hasStringMatcher()) {
      return runCatching {
        Die.valueOf(enumMatcher.nameMatcher.stringMatcher.value)
      }.getOrNull()
    } else if (enumMatcher.numberMatcher.hasValueMatcher()) {
      return runCatching {
        Die.entries[enumMatcher.numberMatcher.valueMatcher.int32Value]
      }.getOrNull()
    }

    throw IllegalArgumentException()
  }
}