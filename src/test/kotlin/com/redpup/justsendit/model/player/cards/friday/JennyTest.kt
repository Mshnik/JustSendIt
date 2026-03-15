package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class JennyTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val jenny = Jenny(playerCard { name = "Jenny" })

  @Test
  fun `handleGameEvent adds points on powder terrain`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { condition = Condition.CONDITION_POWDER },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    jenny.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(1)
  }

  @Test
  fun `handleGameEvent does not add points on other terrain`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { condition = Condition.CONDITION_GROOMED },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    jenny.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(0)
  }
}
