package com.redpup.justsendit.model.board.grid

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexExtensions.distanceTo
import com.redpup.justsendit.model.board.grid.HexExtensions.dq
import com.redpup.justsendit.model.board.grid.HexExtensions.dr
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import org.junit.jupiter.api.Test

class HexExtensionsTest {

  @Test
  fun `test coordinate deltas`() {
    assertThat(HexDirection.HEX_DIRECTION_NORTH.dq()).isEqualTo(0)
    assertThat(HexDirection.HEX_DIRECTION_NORTH.dr()).isEqualTo(-1)
    assertThat(HexDirection.HEX_DIRECTION_SOUTH_EAST.dq()).isEqualTo(1)
    assertThat(HexDirection.HEX_DIRECTION_SOUTH_EAST.dr()).isEqualTo(0)
  }

  @Test
  fun `test down-mountain logic`() {
    assertThat(HexDirection.HEX_DIRECTION_SOUTH.isDownMountain()).isTrue()
    assertThat(HexDirection.HEX_DIRECTION_SOUTH_WEST.isDownMountain()).isTrue()
    assertThat(HexDirection.HEX_DIRECTION_NORTH.isDownMountain()).isFalse()
  }

  @Test
  fun `test hex adjacency operator`() {
    val start = HexPoint(0, 0)
    val north = start + HexDirection.HEX_DIRECTION_NORTH
    assertThat(north.q).isEqualTo(0)
    assertThat(north.r).isEqualTo(-1)

    val southEast = start + HexDirection.HEX_DIRECTION_SOUTH_EAST
    assertThat(southEast.q).isEqualTo(1)
    assertThat(southEast.r).isEqualTo(0)
  }

  @Test
  fun `test distance calculation`() {
    val a = HexPoint(0, 0)
    val b = HexPoint(2, 3) // Manhattan distance in hex grid
    // q:2, r:3, s:-5. Dist = (abs(2)+abs(5)+abs(3))/2 = 5
    assertThat(a.distanceTo(b)).isEqualTo(5)
  }

  @Test
  fun `test pixel conversions`() {
    val p = HexPoint(2, 0)
    assertThat(p.toX()).isEqualTo(3.0) // 2 * 1.5
    assertThat(p.toY()) // (0 + 2/2) * sqrt(3).isEqualTo(Math.sqrt(3.0))
  }
}