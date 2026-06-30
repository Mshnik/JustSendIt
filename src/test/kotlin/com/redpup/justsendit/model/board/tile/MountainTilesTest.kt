package com.redpup.justsendit.model.board.tile

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.tile.MountainTiles.applyHazard
import com.redpup.justsendit.model.board.tile.MountainTiles.applyHazards
import com.redpup.justsendit.model.board.tile.MountainTiles.has
import com.redpup.justsendit.model.board.tile.MountainTiles.matches
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.proto.icon
import kotlin.test.Test

class MountainTilesTest {
  @Test
  fun `has checks conditions`() {
    assertThat(slopeTile {
      condition = Condition.CONDITION_POWDER
    } has Condition.CONDITION_POWDER).isTrue()
    assertThat(slopeTile {
      condition = Condition.CONDITION_POWDER
    } has Condition.CONDITION_ICE).isFalse()
  }

  @Test
  fun `has checks hazards`() {
    assertThat(slopeTile { hazards += Hazard.HAZARD_MOGULS } has Hazard.HAZARD_MOGULS).isTrue()
    assertThat(slopeTile { hazards += Hazard.HAZARD_MOGULS } has Hazard.HAZARD_TREES).isFalse()
  }


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

  @Test
  fun `applyHazard moguls does nothing`() {
    assertThat(listOf(1, 2, 3, 4, 5, 6, 7, 8).map { it.applyHazard(Hazard.HAZARD_MOGULS) })
      .containsExactly(1, 2, 3, 4, 5, 6, 7, 8)
      .inOrder()
  }

  @Test
  fun `applyHazard trees voids 5s`() {
    assertThat(listOf(1, 2, 3, 4, 5, 6, 7, 8).map { it.applyHazard(Hazard.HAZARD_TREES) })
      .containsExactly(1, 2, 3, 4, 0, 6, 7, 8)
      .inOrder()
  }

  @Test
  fun `applyHazard cliffs voids 2s and 3s`() {
    assertThat(listOf(1, 2, 3, 4, 5, 6, 7, 8).map { it.applyHazard(Hazard.HAZARD_CLIFFS) })
      .containsExactly(1, 0, 0, 4, 5, 6, 7, 8)
      .inOrder()
  }

  @Test
  fun `applyHazards applies hazards in order`() {
    assertThat(listOf(1, 2, 3, 4, 5, 6, 7, 8).map {
      it.applyHazards(
        listOf(
          Hazard.HAZARD_MOGULS,
          Hazard.HAZARD_TREES,
          Hazard.HAZARD_CLIFFS
        )
      )
    })
      .containsExactly(1, 0, 0, 4, 0, 6, 7, 8)
      .inOrder()
  }
}