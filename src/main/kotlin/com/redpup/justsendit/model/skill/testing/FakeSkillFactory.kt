package com.redpup.justsendit.model.skill.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.supply.proto.SkillCard
import javax.inject.Singleton

/** A testing implementation of [ApresFactory] */
@VisibleForTesting
@Singleton
class FakeSkillFactory : SkillFactory {
  override val factories: MutableMap<String, (SkillCard) -> Skill> = mutableMapOf()

  /** Registers [name] to [factory]. This overwrites any previous registration for [name]*/
  fun register(name: String, factory: (SkillCard) -> Skill) {
    factories[name] = factory
  }
}