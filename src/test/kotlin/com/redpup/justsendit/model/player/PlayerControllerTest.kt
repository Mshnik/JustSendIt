package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.BasicPlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class PlayerControllerTest {

  private val handler = BasicPlayerController()
  private val player = mock<Player>()
  private val gameModel = mock<GameModel>()

  @Test
  fun `choosePlayerCard returns first card`() {
    val card1 = mock<PlayerCard>()
    val card2 = mock<PlayerCard>()
    val cards = listOf(card1, card2)

    val result = runBlocking { handler.choosePlayerCard(player, cards) }

    assertThat(result).isEqualTo(card1)
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
        handler.getStartingLocation(player, gameModel)
      }
    }
  }

  @Test
  fun `chooseCardsToRemove returns empty list`() {
    val cards = listOf(skillCard { name = "1" }, skillCard { name = "2" })
    val result = runBlocking { handler.chooseCardsToRemove(player, cards, 1) }
    assertThat(result).isEmpty()
  }
}
