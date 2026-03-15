package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class AndyTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val andy = Andy(playerCard { name = "Andy" })

  @Test
  fun `handleGameEvent adds points on speed gain`() {
    player.turn.speed = 2
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    andy.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(2)
  }

  @Test
  fun `handleGameEvent does not add points on crash`() {
    player.turn.speed = 2
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 4,
      difficulty = 5
    )
    andy.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }
}
