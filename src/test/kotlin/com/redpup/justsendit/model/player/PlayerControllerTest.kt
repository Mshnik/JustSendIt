package com.redpup.justsendit.model.player

import com.google.inject.Guice
import com.google.inject.Inject
import com.redpup.justsendit.control.player.BasicPlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class PlayerControllerTest {

  private val handler = BasicPlayerController()
  private val player = mock<Player>()
  private val gameModel = mock<GameModel>()

  @Inject private lateinit var skillFactory: SkillFactory

  @BeforeEach
  fun setup() {
    Guice.createInjector(FakeSkillModule()).injectMembers(this)
  }

  @Test
  fun `choosePlayerCard throws NotImplementedError`() {
    val card1 = mock<PlayerCard>()
    val card2 = mock<PlayerCard>()
    val cards = listOf(card1, card2)

    assertThrows<NotImplementedError> {
      runBlocking {
        handler.choosePlayerCard(player, cards)
      }
    }
  }

  @Test
  fun `makeMountainDecision throws NotImplementedError`() {
    assertThrows<NotImplementedError> {
      runBlocking {
        handler.makeMountainDecision(player, gameModel)
      }
    }
  }

  @Test
  fun `getStartingLocation throws NotImplementedError`() {
    assertThrows<NotImplementedError> {
      runBlocking {
        handler.chooseMountainTile(player, listOf())
      }
    }
  }
}
