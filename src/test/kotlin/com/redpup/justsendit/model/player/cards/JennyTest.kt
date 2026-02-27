package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.playerCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class JennyTest {

  private lateinit var player: MutablePlayer
  private lateinit var jenny: AbilityHandler
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    val playerCard = playerCard { name = "Jenny" }
    player = MutablePlayer(playerCard, mock()) { p -> Jenny(p) }
    jenny = player.abilityHandler
    gameModel = mock()
  }

  @Test
  fun `onCrash returns true if endurance is used`() = runBlocking {
    player.mutate { abilities[0] = true }
    whenever(player.handler.decideToUseEndurance()).thenReturn(true)
    val result = jenny.onCrash(gameModel, -5, true)
    assertThat(result).isTrue()
  }

  @Test
  fun `onGainPoints adds points on powder if ability unlocked`() {
    player.mutate { abilities[1] = true }
    val tileMap = HexGrid<MountainTile>()
    val location = hexPoint { q = 0; r = 0 }
    tileMap[location] = mountainTile {
      slope = slopeTile { condition = Condition.CONDITION_POWDER }
    }
    whenever(gameModel.tileMap).thenReturn(tileMap)
    player.mutate { this.location = location }

    val initialPoints = player.turn.points
    jenny.onGainPoints(5, gameModel)
    assertThat(player.turn.points).isEqualTo(initialPoints + 2)
  }
}
