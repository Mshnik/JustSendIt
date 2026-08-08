package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCard.Computed
import com.redpup.justsendit.model.supply.proto.SkillCardKt.computed
import com.redpup.justsendit.model.supply.proto.SkillCardList
import com.redpup.justsendit.model.supply.proto.copy
import com.redpup.justsendit.util.TextProtoReaderWriterImpl

/** TODO: Description. */
fun main() {
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Shop.textproto")
    .updateComputedFields()
  SkillCalculator("src/main/resources/com/redpup/justsendit/model/shop/skill/Starter.textproto")
    .updateComputedFields()
}

/** TODO: Description */
class SkillCalculator(private val path: String) {
  private val readerWriter = TextProtoReaderWriterImpl(
    path,
    SkillCardList::newBuilder,
    SkillCardList.Builder::getCardsList,
    SkillCardList.Builder::addAllCards,
  )

  /** TODO: Description. */
  fun updateComputedFields() {
    readerWriter.update { it.copy { computed = calculateExpectedValue(it) } }
  }

  /** TODO: Description. */
  private fun calculateExpectedValue(skillCard: SkillCard): Computed {
    return computed {
      diceExpectedValue = 2.0
    }
  }
}