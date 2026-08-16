package com.redpup.justsendit.model.skill

import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.supply.proto.AlterDieEffect
import com.redpup.justsendit.model.supply.proto.GainEffect
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardEffect
import com.redpup.matchers.KMatcher
import javax.inject.Inject

/** Factory for creating [SkillEffect] objects from [SkillCardEffect]s. */
interface SkillEffectFactory {
  /**
   * Creates a [Skill] from an [SkillCard] using this factory.
   *
   * If the SkillCard is not registered but has no effect, we can return a [BaseSkill].
   */
  fun create(card: SkillCardEffect): SkillEffect
}

/** Factory for creating [SkillEffect] objects from [SkillCardEffect]s. */
class SkillEffectFactoryImpl @Inject constructor(private val random: Random) : SkillEffectFactory {
  override fun create(card: SkillCardEffect): SkillEffect {
    when (card.effectCase) {
      SkillCardEffect.EffectCase.ALTER_DIE ->
        with(card.alterDie) {
          val matcher = KMatcher.compile<DieRollOrBuilder>(dieMatcher)
          when (effectCase) {
            AlterDieEffect.EffectCase.REROLL -> ReRollDieEffect(card, random, matcher)
            AlterDieEffect.EffectCase.NUDGE -> NudgeDieEffect(card, matcher)
            AlterDieEffect.EffectCase.EFFECT_NOT_SET, null -> {}
          }
        }

      SkillCardEffect.EffectCase.GAIN -> with (card.gain) {
        when (gainCase) {
          GainEffect.GainCase.SKILL -> GainSkillEffect(card) // TODO: repeat
          GainEffect.GainCase.POINTS -> GainPointsEffect(card)
          GainEffect.GainCase.DIE -> TODO()
          GainEffect.GainCase.BUYS -> TODO()
          GainEffect.GainCase.TRASHES -> TODO()
          GainEffect.GainCase.GAIN_NOT_SET, null -> {}
        }
      }
      SkillCardEffect.EffectCase.IGNORE_WOBBLE -> TODO()
      SkillCardEffect.EffectCase.CARD_EFFECT -> TODO()
      SkillCardEffect.EffectCase.REACTIVATE_FOLLOWING -> TODO()
      SkillCardEffect.EffectCase.FILTER_HAND -> TODO()
      SkillCardEffect.EffectCase.REPLENISH_SHOP -> TODO()
      SkillCardEffect.EffectCase.EXTRA_TURN -> TODO()
      SkillCardEffect.EffectCase.EFFECT_NOT_SET, null -> TODO()
      SkillCardEffect.EffectCase.GAIN_OWN_TAGS -> TODO()
      SkillCardEffect.EffectCase.GAIN_TAGS_BELOW -> TODO()
      SkillCardEffect.EffectCase.DRAW_FROM_PLAY -> TODO()
      SkillCardEffect.EffectCase.MOVE_TILE -> TODO()
      SkillCardEffect.EffectCase.GAIN_FUN_EQUAL_TO_NEXT_CARD_COST -> TODO()
      SkillCardEffect.EffectCase.GAIN_FUN_EQUAL_TO_VALUE_ROLLED -> TODO()
    }

    throw IllegalArgumentException("Unhangled SkillCardEffect: $card")
  }
}