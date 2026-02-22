package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.Clock
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GeorgeTest {

  private lateinit var george: MutablePlayer
  private lateinit var playerController: PlayerController
  private lateinit var gameModel: GameModel
  private lateinit var clock: Clock

  @BeforeEach
  fun setup() {
    playerController = mock()
    val playerCard = playerCard { name = "George" }
    george = MutablePlayer(playerCard, playerController, ::George)
    gameModel = mock()
    clock = mock()
    whenever(gameModel.clock).thenReturn(clock)
  }

  @Test
  fun `computeBonus adds bonus on first turn if ability is unlocked`() {
    george.mutate { abilities[0] = true }
    whenever(clock.turn).thenReturn(1)
    george.abilityHandler.onBeforeTurn(gameModel)
    val bonus = george.computeBonus(slopeTile {})
    assertThat(bonus).isEqualTo(2)
  }

  @Test
  fun `computeBonus adds bonus on last turn if ability is unlocked`() {
    george.mutate { abilities[0] = true }
    whenever(clock.turn).thenReturn(7)
    whenever(clock.maxTurn).thenReturn(7)
    george.abilityHandler.onBeforeTurn(gameModel)
    val bonus = george.computeBonus(slopeTile {})
    assertThat(bonus).isEqualTo(2)
  }

  @Test
  fun `computeBonus does not add bonus if ability is not unlocked`() {
    whenever(clock.turn).thenReturn(1)
    george.abilityHandler.onBeforeTurn(gameModel)
    val bonus = george.computeBonus(slopeTile {})
    assertThat(bonus).isEqualTo(0)
  }

  @Test
  fun `onGainPoints adds points for double black diamond`() {
    george.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = hexPoint { q = 0; r = 0 }
    val tile = mountainTile {
      slope = slopeTile { grade = Grade.GRADE_DOUBLE_BLACK }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    george.mutate { this.location = location }

    val initialPoints = george.turn.points
    george.abilityHandler.onGainPoints(5, gameModel)
    assertThat(george.turn.points).isEqualTo(initialPoints + 5)
  }

  @Test
  fun `onGainPoints adds points for M terrain park`() {
    george.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = hexPoint { q = 0; r = 0 }
    val tile = mountainTile {
      slope = slopeTile {
        terrainPark = terrainPark { size = TerrainPark.Size.SIZE_M }
      }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    george.mutate { this.location = location }

    val initialPoints = george.turn.points
    george.abilityHandler.onGainPoints(5, gameModel)
    assertThat(george.turn.points).isEqualTo(initialPoints + 5)
  }

  @Test
  fun `onGainPoints adds points for L terrain park`() {
    george.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = hexPoint { q = 0; r = 0 }
    val tile = mountainTile {
      slope = slopeTile {
        terrainPark = terrainPark { size = TerrainPark.Size.SIZE_L }
      }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    george.mutate { this.location = location }

    val initialPoints = george.turn.points
    george.abilityHandler.onGainPoints(5, gameModel)
    assertThat(george.turn.points).isEqualTo(initialPoints + 5)
  }

  @Test
  fun `onGainPoints does not add points for other tiles`() {
    george.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = hexPoint { q = 0; r = 0 }
    val tile = mountainTile {
      slope = slopeTile { grade = Grade.GRADE_GREEN }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    george.mutate { this.location = location }

    val initialPoints = george.turn.points
    george.abilityHandler.onGainPoints(5, gameModel)
    assertThat(george.turn.points).isEqualTo(initialPoints)
  }
}
