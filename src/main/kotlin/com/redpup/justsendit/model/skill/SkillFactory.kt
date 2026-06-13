package com.redpup.justsendit.model.skill

import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.supply.proto.SkillCard
import javax.inject.Inject

interface SkillFactory {
  /** Factories registered by name. */
  val factories: Map<String, (SkillCard) -> Skill>

  /**
   * Creates a [Skill] from an [SkillCard] using this factory.
   *
   * If the SkillCard is not registered but has no effect, we can return a [BaseSkill].
   */
  fun create(card: SkillCard): Skill {
    val factory = factories[card.name]
    return if (factory != null) {
      factory(card)
    } else if (card.category == EffectCategory.EFFECT_CATEGORY_UNSET) {
      BaseSkill(card)
    } else {
      throw IllegalArgumentException("No card found for ${card.name} in $factories")
    }
  }
}

/** Factory for creating [Apres] objects from [ApresCard]s. */
class SkillFactoryImpl @Inject constructor() : SkillFactory {
  override val factories: Map<String, (SkillCard) -> Skill> = mapOf()
}
