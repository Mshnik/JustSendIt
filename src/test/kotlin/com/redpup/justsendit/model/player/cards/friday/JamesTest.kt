package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class JamesTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val james = James(playerCard { name = "James" })

  @Test
  fun `handleGameEvent adds points on clean slope`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile {},
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    james.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(1)
  }

  @Test
  fun `handleGameEvent does not add points on slope with moguls`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { hazards += Hazard.HAZARD_MOGULS },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    james.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }

  @Test
  fun `handleGameEvent does not add points on slope with trees`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { hazards += Hazard.HAZARD_TREES },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    james.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }

  @Test
  fun `handleGameEvent does not add points on slope with cliffs`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { hazards += Hazard.HAZARD_CLIFFS },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    james.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }
}
