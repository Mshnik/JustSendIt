package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DavidTest {

  private lateinit var david: MutablePlayer
  private lateinit var playerHandler: PlayerHandler
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    playerHandler = mock()
    val playerCard = playerCard { name = "David" }
    david = MutablePlayer(playerCard, playerHandler, ::David)
    gameModel = mock()
  }

  @Test
  fun `onRest does nothing if ability not unlocked`() {
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    david.abilityHandler.onRest(gameModel)
    assertThat(david.location).isEqualTo(initialLocation)
  }

  @Test
  fun `onRest moves player if ability is unlocked and handler returns direction`() {
    david.mutate { abilities[0] = true }
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    whenever(playerHandler.chooseMoveOnRest(david)).thenReturn(HexDirection.HEX_DIRECTION_NORTH)

    david.abilityHandler.onRest(gameModel)

    val expectedLocation = hexPoint { q = 0; r = -1 }
    assertThat(david.location).isEqualTo(expectedLocation)
  }

  @Test
  fun `onRest does not move player if ability is unlocked and handler returns null`() {
    david.mutate { abilities[0] = true }
    val initialLocation = hexPoint { q = 0; r = 0 }
    david.mutate { location = initialLocation }
    whenever(playerHandler.chooseMoveOnRest(david)).thenReturn(null)

    david.abilityHandler.onRest(gameModel)

    assertThat(david.location).isEqualTo(initialLocation)
  }
}
