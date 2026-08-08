package com.redpup.justsendit.model.skill.calculation

import com.google.protobuf.TextFormat
import com.redpup.justsendit.model.random.NoOpRandom
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCard.Computed
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.model.supply.proto.SkillCardList
import com.redpup.justsendit.model.supply.proto.copy
import com.redpup.justsendit.model.supply.proto.skillCardList
import com.redpup.justsendit.util.TextProtoReaderImpl
import java.io.File

/** TODO: Description. */
fun main() {
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Shop.textproto").updateComputedFields()
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Starter.textproto").updateComputedFields()
}

/** TODO: Description */
class SkillCalculator(private val path: String) {
  private val reader = TextProtoReaderImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    NoOpRandom
  )

  /** TODO: Description. */
  fun updateComputedFields() {
    reader().map { it.copy { computed = calculateExpectedValue(it) } }
      .let { skillCardList { cards += it } }
      .let { File(path).writeText(TextFormat.printer().printToString(it)) }
  }

  /** TODO: Description. */
  private fun calculateExpectedValue(skillCard: SkillCard): Computed {
    return computed {
      diceExpectedValue = 1.0
    }
  }
}