package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class PlayerHandlerTest {

  @Test
  fun `BasicPlayerHandler makeMountainDecision throws NotImplementedError`() {
    val handler = BasicPlayerHandler()
    val player = mock<Player>()
    val gameModel = mock<GameModel>()

    assertThrows<NotImplementedError> {
      handler.makeMountainDecision(player, gameModel)
    }
  }

  @Test
  fun `BasicPlayerHandler getStartingLocation throws NotImplementedError`() {
    val handler = BasicPlayerHandler()
    val player = mock<Player>()
    val gameModel = mock<GameModel>()

    assertThrows<NotImplementedError> {
      handler.getStartingLocation(player, gameModel)
    }
  }
}
