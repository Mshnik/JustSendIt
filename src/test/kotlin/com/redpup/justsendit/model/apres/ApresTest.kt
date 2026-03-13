package com.redpup.justsendit.model.apres

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.supply.SkillDecks
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ApresTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()
  private val skillDecks: SkillDecks = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
    whenever(gameModel.skillDecks).thenReturn(skillDecks)
  }

  @Nested
  inner class BarTest {
    private val bar = Bar(apresCard { name = "Bar" })

    @Test
    fun `first player reveals 5 cards`() {
      player.skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6))
      runBlocking { bar.apply(player, true, gameModel) }
      assertThat(player.day.apresPoints).isIn(
        Range.closed(1 + 2 + 3 + 4 + 5, 2 + 3 + 4 + 5 + 6)
      )
      assertThat(player.skillDeck.size).isEqualTo(1)
      assertThat(player.skillDiscard.size).isEqualTo(5)
    }

    @Test
    fun `other player reveals 3 cards`() {
      player.skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6))
      runBlocking { bar.apply(player, false, gameModel) }
      assertThat(player.day.apresPoints).isIn(
        Range.closed(1 + 2 + 3, 4 + 5 + 6)
      )
      assertThat(player.skillDeck.size).isEqualTo(3)
      assertThat(player.skillDiscard.size).isEqualTo(3)
    }
  }

  @Nested
  inner class DiningTest {
    private val dining = Dining(apresCard { name = "Dining" })

    @Test
    fun `first player gets points for discard size`() {
      player.skillDiscard.addAll(listOf(1, 2, 3))
      runBlocking { dining.apply(player, true, gameModel) }
      assertThat(player.day.apresPoints).isEqualTo(3)
    }

    @Test
    fun `other player gets points for half discard size`() {
      player.skillDiscard.addAll(listOf(1, 2, 3, 4, 5))
      runBlocking { dining.apply(player, false, gameModel) }
      assertThat(player.day.apresPoints).isEqualTo(2)
    }
  }

  @Test
  fun `FirstChair does nothing`() {
    val firstChair = FirstChair(apresCard { name = "First Chair" })
    runBlocking { firstChair.apply(player, true, gameModel) }
    // No assertions, just checking for no exceptions
  }

  @Test
  fun `Journal awards points`() {
    val journal = Journal(apresCard { name = "Journal" })
    runBlocking { journal.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(10)

    player.day.clear()
    runBlocking { journal.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(5)
  }

  @Test
  fun `Massage awards points`() {
    val massage = Massage(apresCard { name = "Massage" })
    runBlocking { massage.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(10)

    player.day.clear()
    runBlocking { massage.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(5)
  }

  @Test
  fun `Study awards points`() {
    val study = Study(apresCard { name = "Study" })
    runBlocking { study.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(10)

    player.day.clear()
    runBlocking { study.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(5)
  }

  @Nested
  inner class TuneUpTest {
    private val tuneUp = TuneUp(apresCard { name = "Tune-Up" })

    @Test
    fun `first player removes up to 3 cards`() {
      player.skillDeck.addAll(listOf(1, 2, 3, 4))
      runBlocking {
        whenever(handler.chooseCardsToRemove(player, player.skillDeck, 3)).thenReturn(listOf(1, 3))
        tuneUp.apply(player, true, gameModel)
      }
      assertThat(player.skillDeck).containsExactly(2, 4).inOrder()
    }

    @Test
    fun `other player removes up to 2 cards`() {
      player.skillDeck.addAll(listOf(1, 2, 3, 4))
      runBlocking {
        whenever(handler.chooseCardsToRemove(player, player.skillDeck, 2)).thenReturn(listOf(2))
        tuneUp.apply(player, false, gameModel)
      }
      assertThat(player.skillDeck).containsExactly(1, 3, 4).inOrder()
    }
  }

  @Nested
  inner class VillageTest {
    private val village = Village(apresCard { name = "Village" })

    @Test
    fun `first player gets points for deck size`() {
      player.skillDeck.addAll(listOf(1, 2, 3))
      runBlocking { village.apply(player, true, gameModel) }
      assertThat(player.day.apresPoints).isEqualTo(3)
    }

    @Test
    fun `other player gets points for half deck size`() {
      player.skillDeck.addAll(listOf(1, 2, 3, 4, 5))
      runBlocking { village.apply(player, false, gameModel) }
      assertThat(player.day.apresPoints).isEqualTo(2)
    }
  }
}
