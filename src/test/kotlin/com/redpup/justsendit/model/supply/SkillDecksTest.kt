package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks.Companion.getSkillGrade
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkillDecksTest {

  @Inject private lateinit var skillDecks: SkillDecksInstance

  @BeforeEach
  fun setup() {
    Guice.createInjector().injectMembers(this)
    skillDecks.reset()
  }

  @Test
  fun `draw returns card of correct grade`() {
    val greenCard = skillDecks.draw(Grade.GRADE_GREEN)
    assertThat(greenCard in 1..3).isTrue()

    val blueCard = skillDecks.draw(Grade.GRADE_BLUE)
    assertThat(blueCard in 4..6).isTrue()

    val blackCard = skillDecks.draw(Grade.GRADE_BLACK)
    assertThat(blackCard in 7..9).isTrue()
  }

  @Test
  fun `getSkillGrade returns correct grade for card value`() {
    assertThat(1.getSkillGrade()).isEqualTo(Grade.GRADE_GREEN)
    assertThat(3.getSkillGrade()).isEqualTo(Grade.GRADE_GREEN)
    assertThat(4.getSkillGrade()).isEqualTo(Grade.GRADE_BLUE)
    assertThat(6.getSkillGrade()).isEqualTo(Grade.GRADE_BLUE)
    assertThat(7.getSkillGrade()).isEqualTo(Grade.GRADE_BLACK)
    assertThat(9.getSkillGrade()).isEqualTo(Grade.GRADE_BLACK)
    assertThat(0.getSkillGrade()).isEqualTo(Grade.GRADE_UNSET)
    assertThat(10.getSkillGrade()).isEqualTo(Grade.GRADE_UNSET)
  }

  @Test
  fun `decks are shuffled and finite`() {
    val greenDeckSize = 16 * 3
    val blueDeckSize = 12 * 3
    val blackDeckSize = 12 * 3

    val greenCards = (1..greenDeckSize).map { skillDecks.draw(Grade.GRADE_GREEN) }
    assertThat(greenCards.size).isEqualTo(greenDeckSize)

    val blueCards = (1..blueDeckSize).map { skillDecks.draw(Grade.GRADE_BLUE) }
    assertThat(blueCards.size).isEqualTo(blueDeckSize)

    val blackCards = (1..blackDeckSize).map { skillDecks.draw(Grade.GRADE_BLACK) }
    assertThat(blackCards.size).isEqualTo(blackDeckSize)

    // Decks should be empty now
    assertThrows(NoSuchElementException::class.java) {
      skillDecks.draw(Grade.GRADE_GREEN)
    }
  }
}
