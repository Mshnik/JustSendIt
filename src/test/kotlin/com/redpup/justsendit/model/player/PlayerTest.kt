package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.grid.HexExtensions
import com.redpup.justsendit.model.player.cards.friday.George
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class PlayerTest {

  private lateinit var player: MutablePlayer
  private val handler = mock<PlayerController>()

  @Inject private lateinit var skillFactory: SkillFactory

  @BeforeEach
  fun setUp() {
    Guice.createInjector(FakeSkillModule()).injectMembers(this)
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
    player.playerCards.add(George(playerCard { name = "Test Player" }))
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
    val card1 = skillFactory.create(skillCard { name = "Card 1" })
    val card2 = skillFactory.create(skillCard { name = "Card 2" })
    val card3 = skillFactory.create(skillCard { name = "Card 3" })
    player.skillDeck.addAll(listOf(card1, card2, card3))
    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(player.skillDiscard.isEmpty()).isTrue()

    val card = player.playSkill()

    assertThat(card).isEqualTo(card1)
    assertThat(player.skillDeck.size).isEqualTo(2)
    assertThat(player.skillDiscard.size).isEqualTo(1)
    assertThat(player.skillDiscard.first()).isEqualTo(card1)
  }

  @Test
  fun `playSkillCard on empty deck returns null`() {
    assertThat(player.skillDeck.isEmpty()).isTrue()
    val card = player.playSkill()
    assertThat(card).isNull()
  }

  @Test
  fun `refreshDecks moves discard to available`() {
    val card1 = skillFactory.create(skillCard { name = "Card 1" })
    val card2 = skillFactory.create(skillCard { name = "Card 2" })
    val card3 = skillFactory.create(skillCard { name = "Card 3" })
    player.skillDiscard.addAll(listOf(card1, card2, card3))

    player.refreshDecks()

    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(player.skillDiscard).isEmpty()
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
  fun `gainSkillCard adds card to deck`() {
    val card = skillFactory.create(skillCard { name = "Test Card" })
    player.gainSkill(card)

    assertThat(player.skillDeck).containsExactly(card)
  }

  @Test
  fun `gainPlayerCard adds card`() = runBlocking {
    val card = George(playerCard {
      name = "Test Card"
    })

    player.gainPlayerCard(card)

    assertThat(player.playerCards).contains(card)
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
