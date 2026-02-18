package com.redpup.justsendit.model.board.tile

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.util.TextProtoReader
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TileMapTest {

  @Test
  fun `constructMap correctly places tiles based on locations`() {
    val greenSlope = mountainTile { slope = slopeTile { grade = Grade.GRADE_GREEN } }
    val blueSlope = mountainTile { slope = slopeTile { grade = Grade.GRADE_BLUE } }
    val liftTile = mountainTile {
      lift = liftTile {
        color = LiftColor.LIFT_COLOR_CYAN; direction = LiftDirection.LIFT_DIRECTION_BOTTOM
      }
    }

    val mockTiles = listOf(greenSlope, blueSlope, liftTile)
    val mockTilesReader = object : TextProtoReader<MountainTile> {
      override fun invoke(): List<MountainTile> = mockTiles
    }

    val greenLocation = mountainTileLocation { point = createHexPoint(0, 1); grade = Grade.GRADE_GREEN }
    val blueLocation = mountainTileLocation { point = createHexPoint(1, 0); grade = Grade.GRADE_BLUE }
    val liftLocation = mountainTileLocation {
      point = createHexPoint(0, 0)
      lift = liftTile {
        color = LiftColor.LIFT_COLOR_CYAN; direction = LiftDirection.LIFT_DIRECTION_BOTTOM
      }
    }

    val mockLocations = listOf(greenLocation, blueLocation, liftLocation)
    val mockLocationsReader = object : TextProtoReader<MountainTileLocation> {
      override fun invoke(): List<MountainTileLocation> = mockLocations
    }

    val grid = TileMap.constructMap(mockTilesReader, mockLocationsReader)

    assertThat(grid.size()).isEqualTo(3)
    assertThat(grid[createHexPoint(0, 1)]).isEqualTo(greenSlope)
    assertThat(grid[createHexPoint(1, 0)]).isEqualTo(blueSlope)
    assertThat(grid[createHexPoint(0, 0)]).isEqualTo(liftTile)
  }

  @Test
  fun `constructMap throws exception for missing slope tile`() {
    val mockTilesReader = object : TextProtoReader<MountainTile> {
      override fun invoke(): List<MountainTile> = emptyList()
    }

    val greenLocation = mountainTileLocation { point = createHexPoint(0, 1); grade = Grade.GRADE_GREEN }
    val mockLocationsReader = object : TextProtoReader<MountainTileLocation> {
      override fun invoke(): List<MountainTileLocation> = listOf(greenLocation)
    }

    assertThrows(IllegalArgumentException::class.java) {
      TileMap.constructMap(mockTilesReader, mockLocationsReader)
    }
  }

  @Test
  fun `constructMap throws exception for missing lift tile`() {
    val mockTilesReader = object : TextProtoReader<MountainTile> {
      override fun invoke(): List<MountainTile> = emptyList()
    }

    val liftLocation = mountainTileLocation {
      point = createHexPoint(0, 0)
      lift = liftTile {
        color = LiftColor.LIFT_COLOR_CYAN; direction = LiftDirection.LIFT_DIRECTION_BOTTOM
      }
    }
    val mockLocationsReader = object : TextProtoReader<MountainTileLocation> {
      override fun invoke(): List<MountainTileLocation> = listOf(liftLocation)
    }

    assertThrows(IllegalArgumentException::class.java) {
      TileMap.constructMap(mockTilesReader, mockLocationsReader)
    }
  }
}
