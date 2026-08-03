package com.redpup.justsendit.model.skill

import com.google.errorprone.annotations.DoNotMock
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardEffect
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
class SkillEffectFactoryImpl @Inject constructor() : SkillEffectFactory {
  override fun create(card: SkillCardEffect): SkillEffect {
    TODO("Not yet implemented")
  }
}