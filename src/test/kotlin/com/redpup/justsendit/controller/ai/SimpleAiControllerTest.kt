package com.redpup.justsendit.controller.ai

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.PlaySkillForLift
import com.redpup.justsendit.control.ai.SimpleAiController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.LiftDirection
import com.redpup.justsendit.model.board.tile.proto.liftTile
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SimpleAiControllerTest {

  private val controller = SimpleAiController("TestAI")

  @Test
  fun makeMountainDecision_exitTile_returnsExit() = runBlocking {
    val location = HexPoint.getDefaultInstance()
    val player = mock<Player> {
      on { this.location } doReturn location
    }
    val tile = mountainTile {
      apresLink = 1
    }
    val tileMap = mock<HexGrid<com.redpup.justsendit.model.board.tile.proto.MountainTile>> {
      on { get(location) } doReturn tile
    }
    val gameModel = mock<GameModel> {
      on { this.tileMap } doReturn tileMap
    }

    val decision = controller.makeMountainDecision(gameModel, player)

    assertThat(decision).isEqualTo(MountainDecision.DECISION_EXIT)
  }

  @Test
  fun makeMountainDecision_liftTile_returnsLift() = runBlocking {
    val location = HexPoint.getDefaultInstance()
    val player = mock<Player> {
      on { this.location } doReturn location
      on { hand } doReturn listOf(mock<Skill>())
    }
    val tile = mountainTile {
      apresLink = 0
      lift = liftTile {
        minCards = 1
        direction = LiftDirection.LIFT_DIRECTION_BOTTOM
      }
    }
    val tileMap = mock<HexGrid<com.redpup.justsendit.model.board.tile.proto.MountainTile>> {
      on { get(location) } doReturn tile
    }
    val gameModel = mock<GameModel> {
      on { this.tileMap } doReturn tileMap
      on { getAvailableMoves(player) } doReturn emptyMap()
    }

    val decision = controller.makeMountainDecision(gameModel, player)

    assertThat(decision).isEqualTo(MountainDecision.DECISION_LIFT)
  }

  @Test
  fun chooseSkillCards_returnsFirstN() = runBlocking {
    val skills = listOf(mock<Skill>(), mock<Skill>(), mock<Skill>())
    val gameModel = mock<GameModel> {
      on { state } doReturn com.redpup.justsendit.model.proto.GameState.BETWEEN_TURNS
    }
    val chosen = controller.chooseSkillCards(
      gameModel,
      mock(),
      PlaySkillForLift,
      skills,
      Range.closed(1, 2)
    )

    assertThat(chosen).hasSize(2)
    assertThat(chosen[0]).isEqualTo(skills[0])
    assertThat(chosen[1]).isEqualTo(skills[1])
  }
}
