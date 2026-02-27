package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.BasicPlayerController
import com.redpup.justsendit.model.GameModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class PlayerControllerTest {

  @Test
  fun `BasicPlayerHandler makeMountainDecision throws NotImplementedError`() {
    val handler = BasicPlayerController()
    val player = mock<Player>()
    val gameModel = mock<GameModel>()

    assertThrows<NotImplementedError> {
      runBlocking {
        handler.makeMountainDecision(player, gameModel)
      }
    }
  }

  @Test
  fun `BasicPlayerHandler getStartingLocation throws NotImplementedError`() {
    val handler = BasicPlayerController()
    val player = mock<Player>()
    val gameModel = mock<GameModel>()

    assertThrows<NotImplementedError> {
      runBlocking {
        handler.getStartingLocation(player, gameModel)
      }
    }
  }
}
