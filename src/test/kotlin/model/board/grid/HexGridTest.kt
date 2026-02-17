package com.redpup.justsendit.model.board.grid

import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HexGridTest {

  @Test
  fun `test basic CRUD operations`() {
    val grid = HexGrid<String>()
    val pt = HexPoint(5, 5)

    assertNull(grid[pt])
    grid[pt] = "Summit"
    assertEquals("Summit", grid[pt])
    assertEquals(1, grid.size())
    assertTrue(grid.contains(pt))

    grid.clear()
    assertEquals(0, grid.size())
  }

  @Test
  fun `test bounds calculation`() {
    val grid = HexGrid<Int>()
    
    grid[HexPoint(0, 0)] = 1
    grid[HexPoint(2, 2)] = 2

    val bounds = grid.bounds()
    assertTrue(bounds.minX <= bounds.maxX)
    assertTrue(bounds.minY <= bounds.maxY)
    assertEquals(4.0, bounds.width) // Based on HexPoint.toX() logic
  }

  @Test
  fun `test iterator`() {
    val grid = HexGrid<Int>()
    grid[HexPoint(0, 0)] = 10
    grid[HexPoint(1, 1)] = 20

    val list = grid.toList()
    assertEquals(2, list.size)
    assertTrue(list.any { it.second == 10 })
  }
}