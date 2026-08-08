package com.redpup.justsendit.model.skill

import com.google.common.collect.Range
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.random.Dice.roll
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.supply.proto.SkillCardEffect
import com.redpup.matchers.KMatcher

/** In-memory implementation of a skill card effect. */
interface SkillEffect {
  /** The underlying [SkillCardEffect] of this [SkillEffect]. */
  val skillCardEffect: SkillCardEffect

  /** Applies the effects of this [SkillEffect] to [SkiRideResolution]. */
  suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  )
}

/** Base no-op impl of [SkillEffect]. */
open class BaseSkillEffect(override val skillCardEffect: SkillCardEffect) : SkillEffect {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
  }
}

/** Skill Effect for re-rolling dice. */
class ReRollDieEffect(
  override val skillCardEffect: SkillCardEffect,
  private val random: Random,
  private val matcher: KMatcher<DieRollOrBuilder>,
) : BaseSkillEffect(skillCardEffect) {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
    player.controller.chooseDice(
      gameModel, player, resolution.rolls, matcher, Range.closed(0, 1)
    ).forEach { it.rollList += it.die.roll(random) }
  }
}

/** Skill Effect for nudging dice. */
class NudgeDieEffect(
  override val skillCardEffect: SkillCardEffect,
  private val matcher: KMatcher<DieRollOrBuilder>,
) : BaseSkillEffect(skillCardEffect) {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
    player.controller.chooseDice(
      gameModel, player, resolution.rolls, matcher, Range.closed(0, 1)
    )
    // TODO: Implement up or down choice for nudging.
  }
}

abstract class GainEffect(override val skillCardEffect: SkillCardEffect) : BaseSkillEffect(skillCardEffect) {
  fun repeatPer(effect: GainEffect): GainEffect {
    TODO()
  }
}

/** Skill Effect for gaining bonus skill. */
class GainSkillEffect(
  override val skillCardEffect: SkillCardEffect,
) : GainEffect(skillCardEffect) {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
    resolution.bonus += skillCardEffect.gain.skill
  }
}

/** Skill Effect for gaining bonus points. */
class GainPointsEffect(
  override val skillCardEffect: SkillCardEffect,
) : GainEffect(skillCardEffect) {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
    player.points += skillCardEffect.gain.points
  }
}

/** Skill Effect for gaining bonus points. */
class IgnoreWobblesEffect(
  override val skillCardEffect: SkillCardEffect,
) : GainEffect(skillCardEffect) {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: MutablePlayer,
    resolution: SkiRideResolution,
  ) {
    resolution.ignoreWobbles++
  }
}
