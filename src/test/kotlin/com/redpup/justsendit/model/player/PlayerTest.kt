package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.grid.HexExtensions
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.proto.trainingChip
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlayerTest {

  private lateinit var player: MutablePlayer
  private val handler = mock<PlayerController>()
  private val skillDecks = mock<SkillDecks>()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  @Test
  fun `mutate applies function to player`() {
    val newLocation = HexExtensions.createHexPoint(1, 2)
    player.mutate { location = newLocation }
    assertThat(player.location).isEqualTo(newLocation)
  }

  @Test
  fun `name is taken from first player card`() {
    assertThat(player.name).isEqualTo("No Name")
    player.playerCards.add(playerCard { name = "Test Player" })
    assertThat(player.name).isEqualTo("Test Player")
  }

  @Test
  fun `isOnMountain checks player location`() {
    player.location = HexExtensions.createHexPoint(0, 1)
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
  fun `playTrainingChip moves chip from available to used`() {
    val chip = trainingChip { condition = Condition.CONDITION_POWDER }
    player.trainingChips.add(chip)
    assertThat(player.trainingChips).contains(chip)
    assertThat(player.usedTrainingChips).doesNotContain(chip)

    player.playTrainingChip(chip)

    assertThat(player.trainingChips).doesNotContain(chip)
    assertThat(player.usedTrainingChips).contains(chip)
  }

  @Test
  fun `refreshDecksAndChips moves discard and used to available`() {
    player.skillDiscard.addAll(listOf(1, 2, 3))
    val chip = trainingChip { condition = Condition.CONDITION_POWDER }
    player.usedTrainingChips.add(chip)

    player.refreshDecksAndChips()

    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(player.skillDiscard).isEmpty()
    assertThat(player.trainingChips).contains(chip)
    assertThat(player.usedTrainingChips).isEmpty()
  }

  @Test
  fun `ingestTurn updates day points from turn points`() {
    player.turn.points = 10
    player.ingestTurn()

    assertThat(player.day.mountainPoints).isEqualTo(10)
    assertThat(player.turn.points).isEqualTo(0)
  }

  @Test
  fun `ingestDayAndCopyNextDay updates player points and resets day`() {
    player.day.mountainPoints = 5
    player.day.bestDayPoints = 10
    player.day.apresPoints = 20

    player.nextDay.mountainPoints = 1
    player.nextDay.bestDayPoints = 2
    player.nextDay.apresPoints = 3

    player.ingestDayAndCopyNextDay()

    assertThat(player.points).isEqualTo(35)
    assertThat(player.day.mountainPoints).isEqualTo(1)
    assertThat(player.day.bestDayPoints).isEqualTo(2)
    assertThat(player.day.apresPoints).isEqualTo(3)
    assertThat(player.nextDay.mountainPoints).isEqualTo(0)
  }

  @Test
  fun `gainSkillCards adds cards to deck`() {
    whenever(skillDecks.draw(Grade.GRADE_GREEN)).thenReturn(1)
    whenever(skillDecks.draw(Grade.GRADE_BLUE)).thenReturn(2)

    player.gainSkillCards(listOf(Grade.GRADE_GREEN, Grade.GRADE_BLUE), skillDecks)

    assertThat(player.skillDeck).containsExactly(1, 2)
  }

  @Test
  fun `gainTrainingChips adds chips`() {
    val chip = trainingChip { condition = Condition.CONDITION_POWDER }
    player.gainTrainingChips(listOf(chip))
    assertThat(player.trainingChips).contains(chip)
  }

  @Test
  fun `gainPlayerCard adds card and its benefits`() = runBlocking {
    val card = playerCard {
      name = "Test Card"
      skillCards += Grade.GRADE_GREEN
      chips += trainingChip { condition = Condition.CONDITION_POWDER }
    }
    whenever(skillDecks.draw(Grade.GRADE_GREEN)).thenReturn(1)

    player.gainPlayerCard(card, skillDecks)

    assertThat(player.playerCards).contains(card)
    assertThat(player.skillDeck).contains(1)
    assertThat(player.trainingChips).contains(trainingChip {
      condition = Condition.CONDITION_POWDER
    })
  }

  @Test
  fun `MutableTurn clear resets points and speed`() {
    val turn = MutableTurn()
    turn.points = 10
    turn.speed = 5
    turn.clear()
    assertThat(turn.points).isEqualTo(0)
    assertThat(turn.speed).isEqualTo(0)
  }

  @Test
  fun `MutableDay clear resets points`() {
    val day = MutableDay()
    day.mountainPoints = 10
    day.bestDayPoints = 5
    day.apresPoints = 2
    day.clear()
    assertThat(day.mountainPoints).isEqualTo(0)
    assertThat(day.bestDayPoints).isEqualTo(0)
    assertThat(day.apresPoints).isEqualTo(0)
  }

  @Test
  fun `MutableDay copyFrom copies points`() {
    val day1 = MutableDay()
    day1.mountainPoints = 10
    day1.bestDayPoints = 5
    day1.apresPoints = 2

    val day2 = MutableDay()
    day2.copyFrom(day1)

    assertThat(day2.mountainPoints).isEqualTo(10)
    assertThat(day2.bestDayPoints).isEqualTo(5)
    assertThat(day2.apresPoints).isEqualTo(2)
  }
}
