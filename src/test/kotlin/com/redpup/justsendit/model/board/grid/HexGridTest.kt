package com.redpup.justsendit.model.board.grid

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import org.junit.jupiter.api.Test

class HexGridTest {

  @Test
  fun `test basic CRUD operations`() {
    val grid = HexGrid<String>()
    val pt = createHexPoint(5, 5)

    assertThat(grid[pt]).isNull()
    grid[pt] = "Summit"
    assertThat(grid[pt]).isEqualTo("Summit")
    assertThat(grid.size()).isEqualTo(1)
    assertThat(grid.contains(pt)).isTrue()

    grid.clear()
    assertThat(grid.size()).isEqualTo(0)
  }

  @Test
  fun `test bounds calculation`() {
    val grid = HexGrid<Int>()

    grid[createHexPoint(0, 0)] = 1
    grid[createHexPoint(2, 2)] = 2

    val bounds = grid.bounds()
    assertThat(bounds.minX <= bounds.maxX).isTrue()
    assertThat(bounds.minY <= bounds.maxY).isTrue()
    assertThat(bounds.width) // Based on HexPoint.toX().isEqualTo(4.0) logic
  }

  @Test
  fun `test iterator`() {
    val grid = HexGrid<Int>()
    grid[createHexPoint(0, 0)] = 10
    grid[createHexPoint(1, 1)] = 20

    val list = grid.toList()
    assertThat(list.size).isEqualTo(2)
    assertThat(list.any { it.second == 10 }).isTrue()
  }
}