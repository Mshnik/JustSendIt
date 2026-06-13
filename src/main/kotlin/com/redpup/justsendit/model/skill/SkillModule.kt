package com.redpup.justsendit.model.skill

import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the skill deck. */
class SkillModule : KtAbstractModule() {
  override fun configure() {
    bind<SkillFactory>().to<SkillFactoryImpl>()
  }
}