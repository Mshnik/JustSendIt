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
    val green = skillCard { greenDice = 1 }
    val blue = skillCard { blueDice = 1 }
    val black = skillCard { blackDice = 1 }
    player.skillDiscard.addAll(listOf(green, green, green, blue, blue, blue, black, black, black))
    runBlocking { iceSkating.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(15) // 3 blues * 5
  }

  @Test
  fun `other player gets points for blue cards`() {
    val green = skillCard { greenDice = 1 }
    val blue = skillCard { blueDice = 1 }
    val black = skillCard { blackDice = 1 }
    player.skillDiscard.addAll(listOf(green, green, green, blue, blue, blue, black, black, black))
    runBlocking { iceSkating.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(9) // 3 blues * 3
  }
}
