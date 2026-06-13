package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class FirePitTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val firePit = FirePit(apresCard { name = "Fire Pit" })

  @Test
  fun `first player gets points for unique cards`() {
    val card1 = skillCard { name = "1" }
    val card2 = skillCard { name = "2" }
    val card3 = skillCard { name = "3" }
    player.skillDiscard.addAll(listOf(card1, card1, card2, card3, card3, card3))
    runBlocking { firePit.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(12) // 3 unique * 4
  }

  @Test
  fun `other player gets points for unique cards`() {
    val card1 = skillCard { name = "1" }
    val card2 = skillCard { name = "2" }
    val card3 = skillCard { name = "3" }
    player.skillDiscard.addAll(listOf(card1, card1, card2, card3, card3, card3))
    runBlocking { firePit.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(6) // 3 unique * 2
  }
}
