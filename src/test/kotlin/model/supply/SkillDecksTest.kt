package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkillDecksTest {

  @BeforeEach
  fun setup() {
    SkillDecksInstance.reset()
  }

  @Test
  fun `draw returns card of correct grade`() {
    val greenCard = SkillDecksInstance.draw(Grade.GRADE_GREEN)
    assertTrue(greenCard in 1..3)

    val blueCard = SkillDecksInstance.draw(Grade.GRADE_BLUE)
    assertTrue(blueCard in 4..6)

    val blackCard = SkillDecksInstance.draw(Grade.GRADE_BLACK)
    assertTrue(blackCard in 7..9)
  }

  @Test
  fun `getSkillGrade returns correct grade for card value`() {
    with(SkillDecksInstance) {
      assertEquals(Grade.GRADE_GREEN, 1.getSkillGrade())
      assertEquals(Grade.GRADE_GREEN, 3.getSkillGrade())
      assertEquals(Grade.GRADE_BLUE, 4.getSkillGrade())
      assertEquals(Grade.GRADE_BLUE, 6.getSkillGrade())
      assertEquals(Grade.GRADE_BLACK, 7.getSkillGrade())
      assertEquals(Grade.GRADE_BLACK, 9.getSkillGrade())
      assertEquals(Grade.GRADE_UNSET, 0.getSkillGrade())
      assertEquals(Grade.GRADE_UNSET, 10.getSkillGrade())
    }
  }

  @Test
  fun `decks are shuffled and finite`() {
    val greenDeckSize = 16 * 3
    val blueDeckSize = 12 * 3
    val blackDeckSize = 12 * 3

    val greenCards = (1..greenDeckSize).map { SkillDecksInstance.draw(Grade.GRADE_GREEN) }
    assertEquals(greenDeckSize, greenCards.size)

    val blueCards = (1..blueDeckSize).map { SkillDecksInstance.draw(Grade.GRADE_BLUE) }
    assertEquals(blueDeckSize, blueCards.size)

    val blackCards = (1..blackDeckSize).map { SkillDecksInstance.draw(Grade.GRADE_BLACK) }
    assertEquals(blackDeckSize, blackCards.size)

    // Decks should be empty now
    assertThrows(NoSuchElementException::class.java) {
      SkillDecksInstance.draw(Grade.GRADE_GREEN)
    }
  }
}
