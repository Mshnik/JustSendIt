package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.player.MutablePlayer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConcertTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val concert = Concert(apresCard { name = "Concert" })

  @Test
  fun `first player gains points for empty tiles`() {
    val tileMapPoints = HexGrid<Int>()
    tileMapPoints[mock()] = 0
    tileMapPoints[mock()] = 1
    tileMapPoints[mock()] = 0
    whenever(gameModel.tileMapPoints).thenReturn(tileMapPoints)

    runBlocking { concert.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(2)
  }

  @Test
  fun `other player gains points for empty tiles`() {
    val tileMapPoints = HexGrid<Int>()
    tileMapPoints[mock()] = 0
    tileMapPoints[mock()] = 1
    tileMapPoints[mock()] = 0
    tileMapPoints[mock()] = 0
    whenever(gameModel.tileMapPoints).thenReturn(tileMapPoints)

    runBlocking { concert.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(1)
  }
}
