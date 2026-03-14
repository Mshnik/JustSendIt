package com.redpup.justsendit.model.apres.cards

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class BarTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val bar = Bar(apresCard { name = "Bar" })

  @Test
  fun `first player reveals 6 cards`() {
    player.skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
    runBlocking { bar.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isIn(
      Range.closed(
        1 + 2 + 3 + 4 + 5 + 6,
        2 + 3 + 4 + 5 + 6 + 7
      )
    )
  }

  @Test
  fun `other player reveals 3 cards`() {
    player.skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
    runBlocking { bar.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isIn(
      Range.closed(1 + 2 + 3, 5 + 6 + 7)
    )
  }
}
