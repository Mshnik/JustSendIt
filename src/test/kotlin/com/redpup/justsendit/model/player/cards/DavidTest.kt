package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.playerCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DavidTest {

  private lateinit var david: MutablePlayer
  private lateinit var playerController: PlayerController
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    playerController = mock()
    val playerCard = playerCard { name = "David" }
    david = MutablePlayer(playerCard, playerController, ::David)
    gameModel = mock()
  }

  @Test
  fun `onRest does nothing if ability not unlocked`()= runBlocking  {
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    david.abilityHandler.onRest(gameModel)
    assertThat(david.location).isEqualTo(initialLocation)
  }

  @Test
  fun `onRest moves player if ability is unlocked and handler returns direction`() = runBlocking {
    david.mutate { abilities[0] = true }
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    whenever(playerController.chooseMoveOnRest(david)).thenReturn(HexDirection.HEX_DIRECTION_NORTH)

    david.abilityHandler.onRest(gameModel)

    val expectedLocation = hexPoint { q = 0; r = -1 }
    assertThat(david.location).isEqualTo(expectedLocation)
  }

  @Test
  fun `onRest does not move player if ability is unlocked and handler returns null`()= runBlocking  {
    david.mutate { abilities[0] = true }
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    whenever(playerController.chooseMoveOnRest(david)).thenReturn(null)

    david.abilityHandler.onRest(gameModel)

    assertThat(david.location).isEqualTo(initialLocation)
  }
}
