package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.proto.Die
import com.redpup.matchers.proto.Matcher

/** Extra utilities for calculating skill card values. */
internal object SkillCalculationUtilities {

  /**
   * Returns which die color a this targets, or `null` for "wild"/any (a `constant_matcher
   * = true` matcher, e.g. "Reroll Wild" / "Slide Wild").
   */
  fun Matcher.dieColorOrWild(): Die? {
    if (hasConstantMatcher() && constantMatcher) {
      return null
    } else if (!hasEnumMatcher()) {
      return null
    }

    val enumMatcher = enumMatcher
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