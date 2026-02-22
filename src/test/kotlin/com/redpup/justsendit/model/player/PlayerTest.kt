package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.Player.Day.OverkillBonus
import com.redpup.justsendit.model.player.proto.playerAbility
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.proto.playerTraining
import com.redpup.justsendit.model.player.proto.playerUpgrade
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks.Companion.getSkillGrade
import com.redpup.justsendit.model.supply.testing.FakeSkillDecks
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class PlayerTest {

  private lateinit var player: MutablePlayer
  private lateinit var skillDecks: FakeSkillDecks
  private val abilityHandler = mock<AbilityHandler>()
  private val gameModel = mock<GameModel>()

  @BeforeEach
  fun setUp() {
    skillDecks = FakeSkillDecks()
    val playerCard = playerCard {
      name = "Test Player"
      upgrades += playerUpgrade {
        cards += Grade.GRADE_GREEN
      }
      upgrades += playerUpgrade {
        cards += Grade.GRADE_BLUE
      }
      training += playerTraining {
        grade = Grade.GRADE_BLUE
        value = 2
      }
      abilities += playerAbility { name = "Test Ability"; cost = 2 }
    }
    player = MutablePlayer(playerCard, mock<PlayerHandler>()) { _ -> abilityHandler }
  }

  @Test
  fun `isOnMountain checks player location`() {
    player.location = createHexPoint(0, 1)
    assertThat(player.isOnMountain).isTrue()

    player.location = null
    assertThat(player.isOnMountain).isFalse()
  }

  @Test
  fun `playSkillCard moves card from deck to discard`() {
    player.skillDeck.addAll(listOf(1, 2, 3))
    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(player.skillDiscard.isEmpty()).isTrue()

    val card = player.playSkillCard()

    assertThat(card).isEqualTo(1)
    assertThat(player.skillDeck.size).isEqualTo(2)
    assertThat(player.skillDiscard.size).isEqualTo(1)
    assertThat(player.skillDiscard.first()).isEqualTo(1)
  }

  @Test
  fun `playSkillCard on empty deck returns null`() {
    assertThat(player.skillDeck.isEmpty()).isTrue()
    val card = player.playSkillCard()
    assertThat(card).isNull()
  }

  @Test
  fun `refreshSkillDeck moves discard to deck and shuffles`() {
    player.skillDiscard.addAll(listOf(1, 2, 3, 4, 5))
    player.refreshSkillDeck()
    assertThat(player.skillDeck.size).isEqualTo(5)
    assertThat(player.skillDiscard.isEmpty()).isTrue()
    // The deck is shuffled, so we can't assert order, but it should contain the elements.
    assertThat(player.skillDeck.containsAll(listOf(1, 2, 3, 4, 5))).isTrue()
  }

  @Test
  fun `ingestTurn updates player stats and clears turn`() {
    player.turn.points = 10
    player.turn.experience = 5
    player.ingestTurn()

    assertThat(player.points).isEqualTo(0)
    assertThat(player.day.mountainPoints).isEqualTo(10)
    assertThat(player.day.experience).isEqualTo(5)
    assertThat(player.turn.points).isEqualTo(0)
    assertThat(player.turn.experience).isEqualTo(0)
  }

  @Test
  fun `ingestDay updates player stats and clears day`() {
    player.day.mountainPoints = 5
    player.day.bestDayPoints = 10
    player.day.apresPoints = 20
    player.ingestDayAndCopyNextDay()

    assertThat(player.points).isEqualTo(35)
    assertThat(player.day.mountainPoints).isEqualTo(0)
    assertThat(player.day.bestDayPoints).isEqualTo(0)
    assertThat(player.day.apresPoints).isEqualTo(0)
  }

  @Test
  fun `ingestDay copies next day`() {
    player.nextDay.overkillBonusPoints = OverkillBonus(5, 4)
    player.nextDay.mountainPoints = 5
    player.nextDay.bestDayPoints = 10
    player.nextDay.apresPoints = 20
    player.ingestDayAndCopyNextDay()

    assertThat(player.points).isEqualTo(0)
    assertThat(player.day.overkillBonusPoints).isEqualTo(OverkillBonus(5, 4))
    assertThat(player.day.mountainPoints).isEqualTo(5)
    assertThat(player.day.bestDayPoints).isEqualTo(10)
    assertThat(player.day.apresPoints).isEqualTo(20)
  }

  @Test
  fun `buyStartingDeck adds correct number of cards`() {
    skillDecks.setGreenDeck(List(5) { 1 })
    skillDecks.setBlueDeck(List(3) { 4 })
    skillDecks.setBlackDeck(List(2) { 7 })
    player.buyStartingDeck(skillDecks)
    assertThat(player.skillDeck.size).isEqualTo(10) // 5 green, 3 blue, 2 black
  }

  @Test
  fun `buyUpgrade requires experience and adds card`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyUpgrade(0, skillDecks)
    }
    player.day.experience = 1
    player.ingestDayAndCopyNextDay()
    skillDecks.setGreenDeck(listOf(1)) // Card to be drawn
    player.buyUpgrade(0, skillDecks)
    assertThat(player.experience).isEqualTo(0)
    assertThat(player.skillDeck.size).isEqualTo(1)
    assertThat(player.skillDeck.first().getSkillGrade()).isEqualTo(Grade.GRADE_GREEN)
  }

  @Test
  fun `buyTraining requires experience and increases training level`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyTraining(0)
    }
    player.day.experience = 1
    player.ingestDayAndCopyNextDay()
    player.buyTraining(0)
    assertThat(player.experience).isEqualTo(0)
    assertThat(player.training[0]).isEqualTo(1)
  }

  @Test
  fun `buyAbility requires experience and unlocks ability`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyAbility(0)
    }
    player.day.experience = 2
    player.ingestDayAndCopyNextDay()
    player.buyAbility(0)
    assertThat(player.experience).isEqualTo(0)
    assertThat(player.abilities[0]).isTrue()
  }

  @Test
  fun `computeBonus calculates bonus correctly`() {
    val blueSlope = slopeTile { grade = Grade.GRADE_BLUE }
    val greenSlope = slopeTile { grade = Grade.GRADE_GREEN }

    // No training level yet
    assertThat(player.computeBonus(blueSlope)).isEqualTo(0)

    player.training[0] = 2 // Training for blue grade has value 2 from playerCard
    assertThat(player.computeBonus(blueSlope)).isEqualTo(4) // 2 * 2
    assertThat(player.computeBonus(greenSlope)).isEqualTo(0)
  }
}
