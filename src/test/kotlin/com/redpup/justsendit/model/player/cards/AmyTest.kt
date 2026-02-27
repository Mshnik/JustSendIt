package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.playerCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AmyTest {

  private lateinit var amy: MutablePlayer
  private lateinit var playerController: PlayerController

  @BeforeEach
  fun setup() {
    playerController = mock()
    val playerCard = playerCard { name = "Amy" }
    amy = MutablePlayer(playerCard, playerController, ::Amy)
  }

  @Test
  fun `onGainSpeed returns true if Be Careful is not unlocked`() = runBlocking {
    assertThat(amy.abilityHandler.onGainSpeed(0)).isTrue()
  }

  @Test
  fun `onGainSpeed calls handler if Be Careful is unlocked`() = runBlocking {
    amy.mutate { abilities[0] = true }
    whenever(playerController.shouldGainSpeed(amy)).thenReturn(false)
    assertThat(amy.abilityHandler.onGainSpeed(0)).isFalse()
  }

  @Test
  fun `getApresPointsMultiplier returns 1 if Bop around town is not unlocked`() {
    assertThat(amy.abilityHandler.getApresPointsMultiplier()).isEqualTo(1)
  }

  @Test
  fun `getApresPointsMultiplier returns 2 if Bop around town is unlocked`() {
    amy.mutate { abilities[1] = true }
    assertThat(amy.abilityHandler.getApresPointsMultiplier()).isEqualTo(2)
  }
}
