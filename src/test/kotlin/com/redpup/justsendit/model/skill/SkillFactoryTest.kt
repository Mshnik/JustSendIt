package com.redpup.justsendit.model.skill

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.supply.proto.skillCard
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SkillFactoryTest {

  private val factory = SkillFactoryImpl()

  @Test
  fun `creates base skill for card with no effect`() {
    val card = skillCard { name = "Basic"; category = EffectCategory.EFFECT_CATEGORY_UNSET }
    val skill = factory.create(card)
    assertThat(skill).isInstanceOf(BaseSkill::class.java)
    assertThat(skill.name).isEqualTo("Basic")
  }

  @Test
  fun `throws exception for unregistered card with effect`() {
    val card = skillCard { name = "Complex"; category = EffectCategory.EFFECT_CATEGORY_PLAY }
    assertThrows(IllegalArgumentException::class.java) {
      factory.create(card)
    }
  }
}
