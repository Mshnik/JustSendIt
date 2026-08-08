package com.redpup.justsendit.model.skill

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.model.proto.EffectCategory
import com.redpup.justsendit.model.random.testing.FakeRandomModule
import com.redpup.justsendit.model.supply.proto.skillCard
import javax.inject.Inject
import kotlin.test.Ignore
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkillFactoryTest {

  @Inject private lateinit var factory: SkillFactory

  @BeforeEach
  fun setup() {
    Guice.createInjector(SkillModule(), FakeRandomModule()).injectMembers(this)
  }

  @Test
  fun `creates base skill for card with no effect`() {
    val card = skillCard { name = "Basic"; category = EffectCategory.EFFECT_CATEGORY_UNSET }
    val skill = factory.create(card)
    assertThat(skill).isInstanceOf(BaseSkill::class.java)
    assertThat(skill.name).isEqualTo("Basic")
  }

  @Test
  @Ignore // TODO
  fun `throws exception for unregistered card with effect`() {
    val card = skillCard { name = "Complex"; category = EffectCategory.EFFECT_CATEGORY_PLAY }
    assertThrows(IllegalArgumentException::class.java) {
      factory.create(card)
    }
  }
}
