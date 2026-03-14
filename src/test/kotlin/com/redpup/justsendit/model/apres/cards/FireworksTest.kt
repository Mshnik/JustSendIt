package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.ApresGameEvent
import com.redpup.justsendit.model.apres.StockpilingBaseApres.Companion.NON_STOCKPILE_POINTS
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class FireworksTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val fireworks = Fireworks(apresCard { name = "Fireworks" })

  @Test
  fun `stockpile increases on playing a 9`() {
    fireworks.handleGameEvent(ApresGameEvent.PlayerPlayedCard(9), gameModel)
    assertThat(fireworks.stockpile).isEqualTo(2)
  }

  @Test
  fun `stockpile does not increase on playing a non-9`() {
    for (card in 1..8) {
      fireworks.handleGameEvent(ApresGameEvent.PlayerPlayedCard(card), gameModel)
    }
    assertThat(fireworks.stockpile).isEqualTo(0)
  }

  @Test
  fun `apply gives stockpile to first player`() {
    fireworks.stockpile = 20
    runBlocking { fireworks.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(20)
    assertThat(fireworks.stockpile).isEqualTo(0)
  }

  @Test
  fun `other player gets non stockpile points`() {
    fireworks.stockpile = 20
    runBlocking { fireworks.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(NON_STOCKPILE_POINTS)
    assertThat(fireworks.stockpile).isEqualTo(20)
  }
}
