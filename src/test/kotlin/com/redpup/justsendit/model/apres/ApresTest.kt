package com.redpup.justsendit.model.apres

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.Player.Day.OverkillBonus
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.supply.testing.FakeSkillDecks
import kotlin.test.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ApresTest {
  private lateinit var player: Player
  private lateinit var skillDecks: FakeSkillDecks
  private lateinit var playerHandler: PlayerHandler

  private lateinit var gameModel: GameModel

  @BeforeEach
  fun setup() {
    skillDecks = FakeSkillDecks()
    // Pre-populate with enough cards for BuyGear tests
    skillDecks.setGreenDeck(List(100) { 1 })
    skillDecks.setBlueDeck(List(100) { 2 })
    skillDecks.setBlackDeck(List(100) { 3 })

    gameModel = mock()
    whenever(gameModel.skillDecks).thenReturn(skillDecks)

    playerHandler = mock()
    player = MutablePlayer(playerCard { name = "Test Player" }, playerHandler)
  }

  @Test
  fun `BuyGear applies correct major reward`() {
    val buyGear = BuyGear(apresCard { name = "Buy Gear" })
    val initialDeckSize = player.skillDeck.size

    player.mutate { buyGear.apply(this, true, gameModel) }

    // 2 blue, 4 black. Total 6 cards.
    assertThat(player.skillDeck.size).isEqualTo(initialDeckSize + 6)
    assertThat(player.skillDeck.filter { it == 2 }).hasSize(2)
    assertThat(player.skillDeck.filter { it == 3 }).hasSize(4)
  }

  @Test
  fun `BuyGear applies correct minor reward`() {
    val buyGear = BuyGear(apresCard { name = "Buy Gear" })
    val initialDeckSize = player.skillDeck.size

    player.mutate { buyGear.apply(this, false, gameModel) }

    // 4 blue, 1 black. Total 5 cards.
    assertThat(player.skillDeck.size).isEqualTo(initialDeckSize + 5)
    assertThat(player.skillDeck.filter { it == 2 }).hasSize(4)
    assertThat(player.skillDeck.filter { it == 3 }).hasSize(1)
  }

  @Test
  fun `Study applies correct major reward`() {
    val study = Study(apresCard { name = "Study" })

    player.mutate { study.apply(this, true, gameModel) }
    assertThat(player.day.experience).isEqualTo(2)
  }

  @Test
  fun `Study applies correct minor reward`() {
    val study = Study(apresCard { name = "Study" })

    player.mutate { study.apply(this, false, gameModel) }
    assertThat(player.day.experience).isEqualTo(1)
  }

  @Test
  @Ignore // TODO later.
  fun `FirstChair applies correct major reward`() {
    val firstChair = FirstChair(apresCard { name = "First Chair" })
  }

  @Test
  @Ignore // TODO later.
  fun `FirstChair applies correct minor reward`() {
    val firstChair = FirstChair(apresCard { name = "First Chair" })
  }

  @Test
  fun `Sauna applies correct major reward`() {
    val sauna = Sauna(apresCard { name = "Sauna" })
    player.mutate {
      sauna.apply(this, true, gameModel)
      ingestDayAndCopyNextDay()
    }
    assertThat(player.day.overkillBonusPoints).isEqualTo(OverkillBonus(5, 4))
  }

  @Test
  fun `Sauna applies correct minor reward`() {
    val sauna = Sauna(apresCard { name = "Sauna" })
    player.mutate {
      sauna.apply(this, false, gameModel)
      ingestDayAndCopyNextDay()
    }
    assertThat(player.day.overkillBonusPoints).isEqualTo(OverkillBonus(5, 2))
  }

  @Test
  fun `Bar applies correct major reward`() {
    val bar = Bar(apresCard { name = "Bar" })
    // Ensure player's skillDeck is populated predictably
    player.mutate {
      skillDeck.clear()
      skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6)) // Add 6 cards
      skillDiscard.clear()
    }
    val initialPoints = player.day.apresPoints

    player.mutate { bar.apply(this, true, gameModel) }

    // Reveals 5 cards, adds their sum to points.
    // Assert value is in range of worst to best.
    assertThat(player.day.apresPoints).isIn(
      Range.closed(
        initialPoints + (1 + 2 + 3 + 4 + 5),
        initialPoints + (2 + 3 + 4 + 5 + 6)
      )
    )
    assertThat(player.skillDeck.size).isEqualTo(1) // 6 initial - 5 revealed
    assertThat(player.skillDiscard.size).isEqualTo(5) // 5 revealed
  }

  @Test
  fun `Bar applies correct minor reward`() {
    val bar = Bar(apresCard { name = "Bar" })
    // Ensure player's skillDeck is populated predictably
    player.mutate {
      skillDeck.clear()
      skillDeck.addAll(listOf(1, 2, 3, 4, 5, 6)) // Add 6 cards
      skillDiscard.clear()
    }
    val initialPoints = player.day.apresPoints

    player.mutate { bar.apply(this, false, gameModel) }

    // Reveals 3 cards, adds their sum to points.
    // Assert value is in range of worst to best.
    assertThat(player.day.apresPoints).isIn(
      Range.closed(
        initialPoints + (1 + 2 + 3),
        initialPoints + (4 + 5 + 6)
      )
    )
    assertThat(player.skillDeck.size).isEqualTo(3) // 6 initial - 3 revealed (remaining in deck are 4, 5, 6)
    assertThat(player.skillDiscard.size).isEqualTo(3) // 3 revealed
  }

  @Test
  fun `Dining applies correct major reward`() {
    val dining = Dining(apresCard { name = "Dining" })

    player.mutate { skillDiscard.addAll(listOf(1, 2, 3, 4, 5)) }
    val initialPoints = player.day.apresPoints

    player.mutate { dining.apply(this, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 5)
  }

  @Test
  fun `Dining applies correct minor reward`() {
    val dining = Dining(apresCard { name = "Dining" })

    player.mutate { skillDiscard.addAll(listOf(1, 2, 3, 4, 5)) }
    val initialPoints = player.day.apresPoints

    player.mutate { dining.apply(this, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 5 / 2) // Integer division
  }

  @Test
  fun `Village applies correct major reward`() {
    val village = Village(apresCard { name = "Village" })

    player.mutate { skillDeck.addAll(listOf(10, 20, 30)) }
    val initialPoints = player.day.apresPoints

    player.mutate { village.apply(this, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 3)
  }

  @Test
  fun `Village applies correct minor reward`() {
    val village = Village(apresCard { name = "Village" })

    player.mutate { skillDeck.addAll(listOf(10, 20, 30)) }
    val initialPoints = player.day.apresPoints

    player.mutate { village.apply(this, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 3 / 2) // Integer division
  }

  @Test
  fun `Massage applies correct major reward`() {
    val massage = Massage(apresCard { name = "Massage" })

    player.mutate { training[0] = 3 } // Simulate 3 experience in training
    val initialPoints = player.day.apresPoints

    player.mutate { massage.apply(this, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 3 * 5)
  }

  @Test
  fun `Massage applies correct minor reward`() {
    val massage = Massage(apresCard { name = "Massage" })

    player.mutate { training[0] = 3 } // Simulate 3 experience in training
    val initialPoints = player.day.apresPoints

    player.mutate { massage.apply(this, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 3 * 2)
  }

  @Test
  fun `Journal applies correct major reward`() {
    val journal = Journal(apresCard { name = "Journal" })

    player.mutate { abilities[0] = true } // Simulate one unlocked ability
    val initialPoints = player.day.apresPoints

    player.mutate { journal.apply(this, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 1 * 10)
  }

  @Test
  fun `Journal applies correct minor reward`() {
    val journal = Journal(apresCard { name = "Journal" })

    player.mutate { abilities[0] = true } // Simulate one unlocked ability
    val initialPoints = player.day.apresPoints

    player.mutate { journal.apply(this, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(initialPoints + 1 * 5)
  }

  @Test
  fun `TuneUp applies correct major reward`() {
    val tuneUp = TuneUp(apresCard { name = "Tune-Up" })

    player.mutate { skillDeck.addAll(listOf(1, 2, 3, 4, 5)) }
    assertThat(player.skillDeck).hasSize(5)

    // Mock playerHandler to return specific cards to remove
    whenever(playerHandler.chooseCardsToRemove(player, player.skillDeck, 3))
      .thenReturn(listOf(1, 3))

    player.mutate { tuneUp.apply(this, true, gameModel) }
    assertThat(player.skillDeck).hasSize(3) // 5 - 2 removed
    assertThat(player.skillDeck).containsExactly(2, 4, 5)
  }

  @Test
  fun `TuneUp applies correct minor reward`() {
    val tuneUp = TuneUp(apresCard { name = "Tune-Up" })

    player.mutate { skillDeck.addAll(listOf(1, 2, 3, 4, 5)) }
    assertThat(player.skillDeck).hasSize(5)

    // Mock playerHandler to return specific cards to remove
    whenever(playerHandler.chooseCardsToRemove(player, player.skillDeck, 2))
      .thenReturn(listOf(2))

    player.mutate { tuneUp.apply(this, false, gameModel) }
    assertThat(player.skillDeck).hasSize(4) // 5 - 1 removed
    assertThat(player.skillDeck).containsExactly(1, 3, 4, 5)
  }
}
