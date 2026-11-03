package com.redpup.justsendit.model.board.tile

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.tile.Hazards.type
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.HazardType
import com.redpup.justsendit.model.board.tile.proto.hazard
import kotlin.test.Test


class HazardsTest {
  @Test
  fun type() {
    assertThat(hazard {
      moguls = Hazard.Moguls.MOGULS_BLUE_TO_GREEN
    }.type()).isEqualTo(HazardType.HAZARD_MOGULS)
    assertThat(hazard {
      moguls = Hazard.Moguls.MOGULS_BLACK_TO_BLUE
    }.type()).isEqualTo(HazardType.HAZARD_MOGULS)
    assertThat(hazard { slow = -1 }.type()).isEqualTo(HazardType.HAZARD_SLOW)
    assertThat(hazard { trees = -1 }.type()).isEqualTo(HazardType.HAZARD_TREES)
    assertThat(hazard { cliffs = -1 }.type()).isEqualTo(HazardType.HAZARD_CLIFFS)
    assertThat(hazard { }.type()).isEqualTo(HazardType.HAZARD_UNSET)
  }
}