package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlaySkillForSkiRideAttempt
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.skill.BaseSkill
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RiskyAiControllerTest {

  @Test
  fun chooseSkillCards_skiRide_lowRisk_picksSafeCard() = runBlocking {
    val controller = RiskyAiController("SafeAI", 0.0)
    val location = HexPoint.getDefaultInstance()
    val slope = slopeTile { difficulty = 5 }

    val skill1 = BaseSkill(skillCard { name = "Weak"; greenDice = 1 }) // EV: 2.5
    val skill2 = BaseSkill(skillCard { name = "Strong"; blackDice = 2 }) // EV: 9.0

    val player = mock<Player> {
      on { this.location } doReturn location
      on { hand } doReturn listOf(skill1, skill2)
      on { inPlay } doReturn emptyList<Skill>()
    }
    val gameModel = mock<GameModel> {}

    val action = controller.chooseSkillCards(
      gameModel,
      player,
      PlaySkillForSkiRideAttempt(slope, 0, 0),
      player.hand,
      Range.closed(0, 1),
      PlayerController.SkillZone.HAND
    )

    // Low risk should pick the strong card to ensure success (needed 5, EV 9.0 vs 2.5)
    assertThat(action.first().name).isEqualTo("Strong")
  }

  @Test
  fun chooseSkillCards_skiRide_highRisk_picksWeakCardIfAcceptable() = runBlocking {
    val controller = RiskyAiController("RiskyAI", 1.0)
    val location = HexPoint.getDefaultInstance()
    val slope = slopeTile { difficulty = 2 }

    val skill1 = BaseSkill(skillCard { name = "Weak"; greenDice = 1 }) // EV: 2.5
    val skill2 = BaseSkill(skillCard { name = "Strong"; blackDice = 2 }) // EV: 9.0

    val player = mock<Player> {
      on { this.location } doReturn location
      on { hand } doReturn listOf(skill1, skill2)
      on { inPlay } doReturn emptyList()
    }
    val gameModel = mock<GameModel> {}

    val action = controller.chooseSkillCards(
      gameModel,
      player,
      PlaySkillForSkiRideAttempt(slope, 0, 0),
      player.hand,
      Range.closed(0, 1),
      PlayerController.SkillZone.HAND
    )

    // High risk should pick the weak card because its EV (2.5) is enough for the difficulty (2)
    // and it wants to save the strong card.
    assertThat(action.first().name).isEqualTo("Weak")
  }
}
