package com.redpup.justsendit.model.skill

import com.google.common.collect.Range
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.random.Dice.roll
import com.redpup.justsendit.model.random.Random
import com.redpup.matchers.KMatcher

/** In-memory implementation of a skill card effect. */
interface SkillEffect {
  /** Applies the effects of this [SkillEffect] to [SkiRideResolution]. */
  suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: Player,
    resolution: SkiRideResolution,
  )
}

/** Skill Effect for re-rolling dice. */
class ReRollDieSkillEffect(
  private val matcher: KMatcher<DieRollOrBuilder>,
  private val random: Random,
) : SkillEffect {
  override suspend fun applySkiRideEffects(
    gameModel: GameModel,
    player: Player,
    resolution: SkiRideResolution,
  ) {
    player.controller.chooseDice(
      gameModel, player, resolution.rolls, matcher, Range.closed(0, 1)
    ).forEach { it.rollList += it.die.roll(random) }
  }
}
