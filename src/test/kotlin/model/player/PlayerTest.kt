package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.proto.PlayerTrainingKt.training
import com.redpup.justsendit.model.player.proto.ability
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.proto.playerTraining
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecksInstance
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class PlayerTest {

  private lateinit var player: MutablePlayer

  @BeforeEach
  fun setUp() {
    val playerCard = playerCard {
      name = "Test Player"
      smallUpgrade.add(Grade.GRADE_GREEN)
      largeUpgrade.add(Grade.GRADE_BLUE)
      training = playerTraining {
        training += training {
          grade = Grade.GRADE_BLUE
          value = 2
        }
      }
      abilities += ability { name = "Test Ability"; cost = 2 }
    }
    player = MutablePlayer(playerCard, Mockito.mock(PlayerHandler::class.java))
  }

  @Test
  fun `playSkillCard moves card from deck to discard`() {
    player.skillDeck.addAll(listOf(1, 2, 3))
    assertEquals(3, player.skillDeck.size)
    assertTrue(player.skillDiscard.isEmpty())

    val card = player.playSkillCard()

    assertEquals(1, card)
    assertEquals(2, player.skillDeck.size)
    assertEquals(1, player.skillDiscard.size)
    assertEquals(1, player.skillDiscard.first())
  }

  @Test
  fun `playSkillCard on empty deck returns null`() {
    assertTrue(player.skillDeck.isEmpty())
    val card = player.playSkillCard()
    assertNull(card)
  }

  @Test
  fun `refreshSkillDeck moves discard to deck and shuffles`() {
    player.skillDiscard.addAll(listOf(1, 2, 3, 4, 5))
    player.refreshSkillDeck()
    assertEquals(5, player.skillDeck.size)
    assertTrue(player.skillDiscard.isEmpty())
    // The deck is shuffled, so we can't assert order, but it should contain the elements.
    assertTrue(player.skillDeck.containsAll(listOf(1, 2, 3, 4, 5)))
  }

  @Test
  fun `ingestTurn updates player stats and clears turn`() {
    player.turn.points = 10
    player.turn.experience = 5
    player.ingestTurn()

    assertEquals(10, player.points)
    assertEquals(5, player.experience)
    assertEquals(0, player.turn.points)
    assertEquals(0, player.turn.experience)
  }

  @Test
  fun `buyStartingDeck adds correct number of cards`() {
    player.buyStartingDeck(SkillDecksInstance)
    assertEquals(10, player.skillDeck.size) // 5 green, 3 blue, 2 black
  }

  @Test
  fun `buySmallUpgrade requires experience and adds card`() {
    assertThrows(IllegalStateException::class.java) {
      player.buySmallUpgrade(SkillDecksInstance)
    }
    player.experience = 1
    player.buySmallUpgrade(SkillDecksInstance)
    assertEquals(0, player.experience)
    assertEquals(1, player.skillDeck.size)
    assertEquals(
      Grade.GRADE_GREEN,
      with(SkillDecksInstance) { player.skillDeck.first().getSkillGrade() })
  }

  @Test
  fun `buyLargeUpgrade requires experience and adds card`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyLargeUpgrade(SkillDecksInstance)
    }
    player.experience = 1
    player.buyLargeUpgrade(SkillDecksInstance)
    assertEquals(0, player.experience)
    assertEquals(1, player.skillDeck.size)
    assertEquals(
      Grade.GRADE_BLUE,
      with(SkillDecksInstance) { player.skillDeck.first().getSkillGrade() })
  }

  @Test
  fun `buyTraining requires experience and increases training level`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyTraining(0)
    }
    player.experience = 1
    player.buyTraining(0)
    assertEquals(0, player.experience)
    assertEquals(1, player.training[0])
  }

  @Test
  fun `buyAbility requires experience and unlocks ability`() {
    assertThrows(IllegalStateException::class.java) {
      player.buyAbility(0)
    }
    player.experience = 2
    player.buyAbility(0)
    assertEquals(0, player.experience)
    assertTrue(player.abilities[0])
  }

  @Test
  fun `computeBonus calculates bonus correctly`() {
    val blueSlope = slopeTile { grade = Grade.GRADE_BLUE }
    val greenSlope = slopeTile { grade = Grade.GRADE_GREEN }

    // No training level yet
    assertEquals(0, player.computeBonus(blueSlope))

    player.training[0] = 2 // Training for blue grade has value 2 from playerCard
    assertEquals(4, player.computeBonus(blueSlope)) // 2 * 2
    assertEquals(0, player.computeBonus(greenSlope))
  }
}
