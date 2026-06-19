package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.random.testing.FakeRandom
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SaunaTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()
  private val random: Random = FakeRandom()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val sauna = Sauna(apresCard { name = "Sauna" })

  @Test
  fun `first player gets points`() {
    // TODO: Update Sauna to Rulebook V2.
    runBlocking { sauna.apply(player, true, gameModel, random) }
    // assertThat(player.points).isEqualTo(...)
  }

  @Test
  fun `other player gets points`() {
    // TODO: Update Sauna to Rulebook V2.
    runBlocking { sauna.apply(player, false, gameModel, random) }
    // assertThat(player.points).isEqualTo(...)
  }
}
