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

class MassageTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val massage = Massage(apresCard { name = "Massage" })

  @Test
  fun `stockpile increases on crash`() {
    massage.handleGameEvent(ApresGameEvent.PlayerSkiRide(1, false), gameModel)
    assertThat(massage.stockpile).isEqualTo(4)
  }

  @Test
  fun `apply gives stockpile to first player`() {
    massage.stockpile = 20
    runBlocking { massage.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(20)
    assertThat(massage.stockpile).isEqualTo(0)
  }

  @Test
  fun `other player gets non stockpile points`() {
    massage.stockpile = 20
    runBlocking { massage.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(NON_STOCKPILE_POINTS)
    assertThat(massage.stockpile).isEqualTo(20)
  }
}
