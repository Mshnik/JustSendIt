package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.board.grid.HexExtensions
import com.redpup.justsendit.model.player.cards.testing.FakePlayerCard
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import com.redpup.justsendit.model.supply.proto.skillCard
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlayerTest {

  private lateinit var player: MutablePlayer
  private val controller = mock<PlayerController>()

  @Inject private lateinit var skillFactory: SkillFactory

  @BeforeEach
  fun setUp() {
    Guice.createInjector(FakeSkillModule()).injectMembers(this)
    player = MutablePlayer(controller)
  }

  @Test
  fun `mutate applies function to player`() {
    val newLocation = HexExtensions.createHexPoint(1, 2)
    player.mutate { location = newLocation }
    assertThat(player.location).isEqualTo(newLocation)
  }

  @Test
  fun `name is taken from first player card or controller`() {
    whenever(controller.name).thenReturn("ControllerName")
    assertThat(player.name).isEqualTo("ControllerName")
    player.playerCards.add(FakePlayerCard(playerCard { name = "Test Player" }))
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
  fun `gainSkillCard adds card to deck`() {
    val card = skillFactory.create(skillCard { name = "Test Card" })
    player.gainSkill(card)

    assertThat(player.skillDeck).containsExactly(card)
  }

  @Test
  fun `gainPlayerCard adds card`() = runBlocking {
    val card = FakePlayerCard(playerCard {
      name = "Test Card"
    })

    player.gainPlayerCard(card)

    assertThat(player.playerCards).contains(card)
  }
}
