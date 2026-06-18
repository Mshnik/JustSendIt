package com.redpup.justsendit.model.skill

import com.google.errorprone.annotations.DoNotMock
import com.redpup.justsendit.model.supply.proto.SkillCard

/** In-memory implementation of a skill card. */
@DoNotMock(value = "Use FakeSkill instead.")
interface Skill {
  /** The underlying [SkillCard] of this [Skill]. */
  val skillCard: SkillCard

  /** The name of this [Skill]. */
  val name: String get() = skillCard.name
}

/** Base implementation of a Skill card with no effect. */
open class BaseSkill(override val skillCard: SkillCard) : Skill
