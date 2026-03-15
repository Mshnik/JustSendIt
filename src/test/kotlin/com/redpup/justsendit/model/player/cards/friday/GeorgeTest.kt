package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class GeorgeTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val george = George(playerCard { name = "George" })

  @Test
  fun `handleGameEvent adds points on fresh slope`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    george.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(1)
  }

  @Test
  fun `handleGameEvent does not add points on non-fresh slope`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 0,
      skill = 10,
      difficulty = 5
    )
    george.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }
}
