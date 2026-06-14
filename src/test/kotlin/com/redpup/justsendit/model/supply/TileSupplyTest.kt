package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.random.testing.FakeRandom
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class TileSupplyTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var tileFile: File
  private lateinit var locationFile: File
  private val random = FakeRandom()

  @BeforeEach
  fun setup() {
    tileFile = File(tempDir, "tiles.textproto")
    tileFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/board/tile/tile.proto
            # proto-message: MountainTileList

            tiles {
              slope { grade: GRADE_GREEN }
            }
        """.trimIndent()
    )

    locationFile = File(tempDir, "locations.textproto")
    locationFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/board/tile/tile.proto
            # proto-message: MountainTileLocationList

            location {
              point { q: 0 r: 0 }
              grade: GRADE_GREEN
            }
        """.trimIndent()
    )
  }

  @Test
  fun `reads tiles and locations from files`() {
    val supply = TileSupplyImpl(tileFile.absolutePath, locationFile.absolutePath, random)

    assertThat(supply.tiles).hasSize(1)
    assertThat(supply.tiles[0].hasSlope()).isTrue()

    assertThat(supply.locations).hasSize(1)
    assertThat(supply.locations[0].point.q).isEqualTo(0)
  }
}
