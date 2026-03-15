package com.redpup.justsendit.model.player.cards.friday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.playerCard
import org.mockito.kotlin.mock

class CourtneyTest {

  private val player = MutablePlayer(mock())
  private val gameModel = mock<GameModel>()
  private val courtney = Courtney(playerCard { name = "Courtney" })

  // TODO: uncomment once terrain parks are implemented.
  // @Test
  // fun `handleGameEvent adds points on terrain park`() {
  //   val event = PlayerGameEvent.PlayerSkiRide(
  //     turn = 1,
  //     slope = slopeTile { isTerrainPark = true },
  //     pointsOnSlope = 5,
  //     skill = 10,
  //     difficulty = 5
  //   )
  //   courtney.handleGameEvent(event, player, gameModel)
  //   assertThat(player.turn.points).isEqualTo(2)
  // }
  //
  // @Test
  // fun `handleGameEvent does not add points on non-terrain park`() {
  //   val event = PlayerGameEvent.PlayerSkiRide(
  //     turn = 1,
  //     slope = slopeTile { isTerrainPark = false },
  //     pointsOnSlope = 5,
  //     skill = 10,
  //     difficulty = 5
  //   )
  //   courtney.handleGameEvent(event, player, gameModel)
  //   assertThat(player.turn.points).isEqualTo(0)
  // }
}
