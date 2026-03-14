package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class DiningTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val dining = Dining(apresCard { name = "Dining" })

  @Test
  fun `first player gets points for pairs`() {
    player.skillDiscard.addAll(listOf(1, 1, 2, 2, 2, 3))
    runBlocking { dining.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(14) // 2 pairs * 7
  }

  @Test
  fun `other player gets points for pairs`() {
    player.skillDiscard.addAll(listOf(1, 1, 2, 2, 2, 3))
    runBlocking { dining.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(8) // 2 pairs * 4
  }
}
