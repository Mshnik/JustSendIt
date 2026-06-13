package com.redpup.justsendit.model.skill.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.skill.BaseSkill
import com.redpup.justsendit.model.supply.proto.SkillCard


/** A testing implementation of an Apres card. */
@VisibleForTesting
class FakeSkill(override val skillCard: SkillCard) : BaseSkill(skillCard)