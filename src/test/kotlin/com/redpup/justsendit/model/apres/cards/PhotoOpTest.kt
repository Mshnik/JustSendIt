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

class PhotoOpTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val photoOp = PhotoOp(apresCard { name = "Photo-Op" })

  @Test
  fun `first player gets points for speed`() {
    player.turn.speed = 3
    runBlocking { photoOp.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(18) // 3 speed * 6
  }
}
