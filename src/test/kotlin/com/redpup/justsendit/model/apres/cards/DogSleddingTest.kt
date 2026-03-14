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

class DogSleddingTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val dogSledding = DogSledding(apresCard { name = "Dog Sledding" })

  @Test
  fun `stockpile increases on first turn success`() {
    dogSledding.handleGameEvent(ApresGameEvent.PlayerSkiRide(1, true), gameModel)
    dogSledding.handleGameEvent(ApresGameEvent.PlayerSkiRide(2, true), gameModel)
    dogSledding.handleGameEvent(ApresGameEvent.PlayerSkiRide(1, false), gameModel)
    assertThat(dogSledding.stockpile).isEqualTo(4)
  }

  @Test
  fun `stockpile does not increase on non-first turn or non-success`() {
    dogSledding.handleGameEvent(ApresGameEvent.PlayerSkiRide(2, true), gameModel)
    dogSledding.handleGameEvent(ApresGameEvent.PlayerSkiRide(1, false), gameModel)
    assertThat(dogSledding.stockpile).isEqualTo(0)
  }

  @Test
  fun `apply gives stockpile to first player`() {
    dogSledding.stockpile = 20
    runBlocking { dogSledding.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(20)
    assertThat(dogSledding.stockpile).isEqualTo(0)
  }

  @Test
  fun `other player gets NON_STOCKPILE_POINTS points`() {
    dogSledding.stockpile = 20
    runBlocking { dogSledding.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(NON_STOCKPILE_POINTS)
    assertThat(dogSledding.stockpile).isEqualTo(20)
  }
}
