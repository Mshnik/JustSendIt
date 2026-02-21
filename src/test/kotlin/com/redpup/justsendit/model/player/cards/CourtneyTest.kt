package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.board.tile.proto.terrainPark
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourtneyTest {

  private lateinit var courtney: MutablePlayer
  private lateinit var playerHandler: PlayerHandler
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    playerHandler = mock()
    val playerCard = playerCard { name = "Courtney" }
    courtney = MutablePlayer(playerCard, playerHandler, ::Courtney)
    gameModel = mock()
  }

  @Test
  fun `ignoresSlowZones returns false if ability not unlocked`() {
    assertThat(courtney.abilityHandler.ignoresSlowZones()).isFalse()
  }

  @Test
  fun `ignoresSlowZones returns true if ability unlocked`() {
    courtney.mutate { abilities[0] = true }
    assertThat(courtney.abilityHandler.ignoresSlowZones()).isTrue()
  }

  @Test
  fun `onGainPoints does nothing if ability not unlocked`() {
    val tileMap = HexGrid<MountainTile>()
    whenever(gameModel.tileMap).thenReturn(tileMap)
    val initialPoints = courtney.turn.points
    courtney.mutate { location = createHexPoint(0, 0) }
    courtney.abilityHandler.onGainPoints(5, gameModel)
    assertThat(courtney.turn.points).isEqualTo(initialPoints)
  }

  @Test
  fun `onGainPoints adds points if ability unlocked and on terrain park`() {
    courtney.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = createHexPoint(0, 0)
    val tile = mountainTile {
      slope = slopeTile {
        terrainPark = terrainPark { }
      }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    courtney.mutate { this.location = location }


    val initialPoints = courtney.turn.points
    courtney.abilityHandler.onGainPoints(5, gameModel)
    assertThat(courtney.turn.points).isEqualTo(initialPoints + 4)
  }

  @Test
  fun `onGainPoints does nothing if ability unlocked and not on terrain park`() {
    courtney.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = createHexPoint(0, 0)
    val tile = mountainTile {
      slope = slopeTile { }
    }
    tileMap[location] = tile
    whenever(gameModel.tileMap).thenReturn(tileMap)
    courtney.mutate { this.location = location }

    val initialPoints = courtney.turn.points
    courtney.abilityHandler.onGainPoints(5, gameModel)
    assertThat(courtney.turn.points).isEqualTo(initialPoints)
  }
}
