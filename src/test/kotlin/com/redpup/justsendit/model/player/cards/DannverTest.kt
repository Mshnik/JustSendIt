package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.playerCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class DannverTest {

  private lateinit var dannver: MutablePlayer
  private lateinit var playerController: PlayerController
  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    playerController = mock()
    val playerCard = playerCard { name = "Dannver" }
    dannver = MutablePlayer(playerCard, playerController, ::Dannver)
    gameModel = mock()
  }

  @Test
  fun `onCrash does nothing if ability not unlocked`()= runBlocking  {
    val initialExperience = dannver.turn.experience
    dannver.abilityHandler.onCrash(gameModel, -1, true)
    assertThat(dannver.turn.experience).isEqualTo(initialExperience)
  }

  @Test
  fun `onCrash does nothing if ability is unlocked and is not wipeout`()= runBlocking  {
    dannver.mutate { abilities[0] = true }
    val initialExperience = dannver.turn.experience
    dannver.abilityHandler.onCrash(gameModel, -1, false)
    assertThat(dannver.turn.experience).isEqualTo(initialExperience)
  }

  @Test
  fun `onCrash gains experience if ability is unlocked and is wipeout`()= runBlocking  {
    dannver.mutate { abilities[0] = true }
    val initialExperience = dannver.turn.experience
    dannver.abilityHandler.onCrash(gameModel, -1, true)
    assertThat(dannver.turn.experience).isEqualTo(initialExperience + 1)
  }

  @Test
  fun `onGainPoints does nothing if ability not unlocked`() {
    val initialPoints = dannver.turn.points
    dannver.abilityHandler.onGainPoints(5, gameModel)
    assertThat(dannver.turn.points).isEqualTo(initialPoints)
  }

  @Test
  fun `onGainPoints adds points if ability unlocked and points gained is 5 or more`() {
    dannver.mutate { abilities[1] = true }
    val initialPoints = dannver.turn.points
    dannver.abilityHandler.onGainPoints(5, gameModel)
    assertThat(dannver.turn.points).isEqualTo(initialPoints + 2)
  }

  @Test
  fun `onGainPoints does nothing if ability unlocked and points gained is less than 5`() {
    dannver.mutate { abilities[1] = true }
    val initialPoints = dannver.turn.points
    dannver.abilityHandler.onGainPoints(4, gameModel)
    assertThat(dannver.turn.points).isEqualTo(initialPoints)
  }
}
