package com.redpup.justsendit.model.apres.cards

import com.google.common.collect.Range
import com.redpup.justsendit.control.Choice
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.random.testing.FakeRandom
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class LodgeTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()
  private val random: Random = FakeRandom()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val lodge = Lodge(apresCard { name = "Lodge" })

  @Test
  fun `first player chooses 2 other apres`() = runBlocking {
    val otherApres = listOf<Apres>(mock(), mock())
    whenever(gameModel.apres).thenReturn(otherApres + lodge)
    whenever(handler.choose(any(), any(), any<List<Apres>>(), any())).thenReturn(otherApres.take(2))

    lodge.apply(player, true, gameModel, random)
    verify(handler).choose(Choice.ApresCard, player, otherApres, Range.closed(2, 2))
    verify(otherApres[0]).apply(player, false, gameModel, random)
    verify(otherApres[1]).apply(player, false, gameModel, random)
  }

  @Test
  fun `other player chooses 1 other apres`() = runBlocking {
    val otherApres = listOf<Apres>(mock(), mock())
    whenever(gameModel.apres).thenReturn(otherApres + lodge)
    whenever(handler.choose(any(), any(), any<List<Apres>>(), any())).thenReturn(otherApres.take(1))

    lodge.apply(player, false, gameModel, random)
    verify(handler).choose(Choice.ApresCard, player, otherApres, Range.closed(1, 1))
    verify(otherApres[0]).apply(player, false, gameModel, random)
    verify(otherApres[1], never()).apply(any(), any(), any(), any())
  }
}
