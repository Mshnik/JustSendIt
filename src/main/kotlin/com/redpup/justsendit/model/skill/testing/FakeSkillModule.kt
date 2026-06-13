package com.redpup.justsendit.model.skill.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.util.KtAbstractModule

/** Testing Binding module for the apres deck. */
@VisibleForTesting
class FakeSkillModule : KtAbstractModule() {
  override fun configure() {
    bind<SkillFactory>().to<FakeSkillFactory>()
  }
}