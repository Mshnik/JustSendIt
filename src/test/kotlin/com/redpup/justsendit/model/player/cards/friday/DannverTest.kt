package com.redpup.justsendit.model.player.cards.friday

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class DannverTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val dannver = Dannver(playerCard { name = "Dannver" })

  @BeforeEach
  fun setUp() {
    dannver.startTurn()
  }

  @Test
  fun `handleGameEvent adds points on new grade`() {
    val event = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { grade = Grade.GRADE_GREEN },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    dannver.handleGameEvent(event, player, gameModel)
    assertThat(player.turn.points).isEqualTo(1)
  }

  @Test
  fun `handleGameEvent does not add points on same grade`() {
    val event1 = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { grade = Grade.GRADE_GREEN },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    dannver.handleGameEvent(event1, player, gameModel)

    val event2 = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { grade = Grade.GRADE_GREEN },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    dannver.handleGameEvent(event2, player, gameModel)

    assertThat(player.turn.points).isEqualTo(1) // only 1 point from the first ride
  }

  @Test
  fun `startTurn resets ridden grades`() {
    val event1 = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { grade = Grade.GRADE_GREEN },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    dannver.handleGameEvent(event1, player, gameModel)

    dannver.startTurn()

    val event2 = PlayerGameEvent.PlayerSkiRide(
      turn = 1,
      slope = slopeTile { grade = Grade.GRADE_GREEN },
      pointsOnSlope = 5,
      skill = 10,
      difficulty = 5
    )
    dannver.handleGameEvent(event2, player, gameModel)

    assertThat(player.turn.points).isEqualTo(2) // 1 from before reset, 1 from after
  }
}
