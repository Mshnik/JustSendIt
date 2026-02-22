package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class AndyTest {

  private lateinit var andy: MutablePlayer
  private lateinit var playerController: PlayerController
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    playerController = mock()
    val playerCard = playerCard { name = "Andy" }
    andy = MutablePlayer(playerCard, playerController, ::Andy)
    gameModel = mock()
  }

  @Test
  fun `onCrash returns false if ability is not unlocked`() {
    assertThat(andy.abilityHandler.onCrash(gameModel, -2, false)).isFalse()
  }

  @Test
  fun `onCrash returns true if ability is unlocked and diff is -2`() {
    andy.mutate { abilities[0] = true }
    assertThat(andy.abilityHandler.onCrash(gameModel, -2, false)).isTrue()
  }

  @Test
  fun `onCrash returns false if ability is unlocked and diff is less than -2`() {
    andy.mutate { abilities[0] = true }
    assertThat(andy.abilityHandler.onCrash(gameModel, -3, false)).isFalse()
  }

  @Test
  fun `onGainSpeed does nothing if ability is not unlocked`() {
    val initialPoints = andy.turn.points
    andy.abilityHandler.onGainSpeed(2)
    assertThat(andy.turn.points).isEqualTo(initialPoints)
  }

  @Test
  fun `onGainSpeed adds points if ability is unlocked and speed is 2`() {
    andy.mutate { abilities[1] = true }
    val initialPoints = andy.turn.points
    andy.abilityHandler.onGainSpeed(2)
    assertThat(andy.turn.points).isEqualTo(initialPoints + 5)
  }

  @Test
  fun `onGainSpeed does nothing if ability is unlocked and speed is not 2`() {
    andy.mutate { abilities[1] = true }
    val initialPoints = andy.turn.points
    andy.abilityHandler.onGainSpeed(1)
    assertThat(andy.turn.points).isEqualTo(initialPoints)
    andy.abilityHandler.onGainSpeed(3)
    assertThat(andy.turn.points).isEqualTo(initialPoints)
  }
}
