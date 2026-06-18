package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.board.tile.proto.liftTile
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

    val decision = controller.makeMountainDecision(player, gameModel)

    assertThat(decision.decisionCase).isEqualTo(MountainDecision.DecisionCase.EXIT)
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
      }
    }
    val tileMap = mock<HexGrid<com.redpup.justsendit.model.board.tile.proto.MountainTile>> {
      on { get(location) } doReturn tile
    }
    val gameModel = mock<GameModel> {
      on { this.tileMap } doReturn tileMap
    }

    val decision = controller.makeMountainDecision(player, gameModel)

    assertThat(decision.decisionCase).isEqualTo(MountainDecision.DecisionCase.LIFT)
  }

  @Test
  fun chooseSkillCards_returnsFirstN() = runBlocking {
    val skills = listOf(mock<Skill>(), mock<Skill>(), mock<Skill>())
    val chosen = controller.chooseSkillCards(mock(), skills, Range.closed(1, 2))
    
    assertThat(chosen).hasSize(2)
    assertThat(chosen[0]).isEqualTo(skills[0])
    assertThat(chosen[1]).isEqualTo(skills[1])
  }
}
