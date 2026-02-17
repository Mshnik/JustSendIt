package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class PlayerHandlerTest {

  @Test
  fun `BasicPlayerHandler makeMountainDecision throws NotImplementedError`() {
    val handler = BasicPlayerHandler()
    val player = mock(Player::class.java)
    val gameModel = mock(GameModel::class.java)

    assertThrows<NotImplementedError> {
      handler.makeMountainDecision(player, gameModel)
    }
  }

  @Test
  fun `BasicPlayerHandler getStartingLocation throws NotImplementedError`() {
    val handler = BasicPlayerHandler()
    val player = mock(Player::class.java)
    val gameModel = mock(GameModel::class.java)

    assertThrows<NotImplementedError> {
      handler.getStartingLocation(player, gameModel)
    }
  }
}
