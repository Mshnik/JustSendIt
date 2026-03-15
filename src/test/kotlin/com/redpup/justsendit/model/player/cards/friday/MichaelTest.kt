package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MichaelTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val michael = Michael(playerCard { name = "Michael" })

  @Test
  fun `handleGameEvent adds points on exact skill match`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 10
    )
    michael.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(3)
  }

  @Test
  fun `handleGameEvent does not add points on non-exact match`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 11,
      difficulty = 10
    )
    michael.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }
}
