package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.Icons.matches
import com.redpup.justsendit.model.supply.proto.icon
import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.Test

class IconsTest {

  @Test
  fun `matches grade`() {
    val greenIcon = icon { grade = Grade.GRADE_GREEN }
    val greenSlope = slopeTile { grade = Grade.GRADE_GREEN }
    val blueSlope = slopeTile { grade = Grade.GRADE_BLUE }

    assertThat(greenIcon.matches(greenSlope)).isTrue()
    assertThat(greenIcon.matches(blueSlope)).isFalse()
  }

  @Test
  fun `matches condition`() {
    val powderIcon = icon { condition = Condition.CONDITION_POWDER }
    val powderSlope = slopeTile { condition = Condition.CONDITION_POWDER }
    val icySlope = slopeTile { condition = Condition.CONDITION_ICE }

    assertThat(powderIcon.matches(powderSlope)).isTrue()
    assertThat(powderIcon.matches(icySlope)).isFalse()
  }

  @Test
  fun `matches hazard`() {
    val treesIcon = icon { hazard = Hazard.HAZARD_TREES }
    val treesSlope = slopeTile { hazards += Hazard.HAZARD_TREES }
    val mogulsSlope = slopeTile { hazards += Hazard.HAZARD_MOGULS }

    assertThat(treesIcon.matches(treesSlope)).isTrue()
    assertThat(treesIcon.matches(mogulsSlope)).isFalse()
  }

  @Test
  fun `matches wild`() {
    val wildIcon = icon { wild = true }
    val anySlope = slopeTile { grade = Grade.GRADE_BLACK }

    assertThat(wildIcon.matches(anySlope)).isTrue()
  }

  @Test
  fun `non-matching icon`() {
    val unsetIcon = icon { }
    val slope = slopeTile { grade = Grade.GRADE_GREEN }

    assertThat(unsetIcon.matches(slope)).isFalse()
  }
}
