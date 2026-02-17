package com.redpup.justsendit.model.board.grid

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.grid.HexExtensions.distanceTo
import com.redpup.justsendit.model.board.grid.HexExtensions.dq
import com.redpup.justsendit.model.board.grid.HexExtensions.dr
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HexExtensionsTest {

  @Test
  fun `test coordinate deltas`() {
    assertEquals(0, HexDirection.HEX_DIRECTION_NORTH.dq())
    assertEquals(-1, HexDirection.HEX_DIRECTION_NORTH.dr())
    assertEquals(1, HexDirection.HEX_DIRECTION_SOUTH_EAST.dq())
    assertEquals(0, HexDirection.HEX_DIRECTION_SOUTH_EAST.dr())
  }

  @Test
  fun `test down-mountain logic`() {
    assertTrue(HexDirection.HEX_DIRECTION_SOUTH.isDownMountain())
    assertTrue(HexDirection.HEX_DIRECTION_SOUTH_WEST.isDownMountain())
    assertFalse(HexDirection.HEX_DIRECTION_NORTH.isDownMountain())
  }

  @Test
  fun `test hex adjacency operator`() {
    val start = HexPoint(0, 0)
    val north = start + HexDirection.HEX_DIRECTION_NORTH
    assertEquals(0, north.q)
    assertEquals(-1, north.r)

    val southEast = start + HexDirection.HEX_DIRECTION_SOUTH_EAST
    assertEquals(1, southEast.q)
    assertEquals(0, southEast.r)
  }

  @Test
  fun `test distance calculation`() {
    val a = HexPoint(0, 0)
    val b = HexPoint(2, 3) // Manhattan distance in hex grid
    // q:2, r:3, s:-5. Dist = (abs(2)+abs(5)+abs(3))/2 = 5
    assertEquals(5, a.distanceTo(b))
  }

  @Test
  fun `test pixel conversions`() {
    val p = HexPoint(2, 0)
    assertEquals(3.0, p.toX()) // 2 * 1.5
    assertEquals(Math.sqrt(3.0), p.toY()) // (0 + 2/2) * sqrt(3)
  }
}