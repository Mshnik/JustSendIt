package com.redpup.justsendit.model.skill

import com.redpup.justsendit.model.supply.proto.SkillCard
import javax.inject.Inject

/** Builder factory converting proto [SkillCard] into in-memory [Skill]s. */
interface SkillFactory {
  /**
   * Creates a [Skill] from an [SkillCard] using this factory.
   *
   * If the SkillCard is not registered but has no effect, we can return a [BaseSkill].
   */
  fun create(card: SkillCard): Skill
}

/** Factory for creating [Skill] objects from [SkillCard]s. */
class SkillFactoryImpl @Inject constructor(private val skillEffectFactory: SkillEffectFactory) :
  SkillFactory {
  override fun create(card: SkillCard): Skill =
    BaseSkill(card, card.effectsList.map { skillEffectFactory.create(it) })
}
