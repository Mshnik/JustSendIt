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

class IceSkatingTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val iceSkating = IceSkating(apresCard { name = "Ice Skating" })

  @Test
  fun `first player gets points for blue cards`() {
    player.skillDiscard.addAll(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
    runBlocking { iceSkating.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(15) // 3 blues * 5
  }

  @Test
  fun `other player gets points for blue cards`() {
    player.skillDiscard.addAll(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
    runBlocking { iceSkating.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(9) // 3 blues * 3
  }
}
