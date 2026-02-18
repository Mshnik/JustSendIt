package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.proto.Grade

/** Access to skill decks in the supply. */
interface SkillDecks {
  /** Resets this skill decks to its starting state. */
  fun reset()

  /** Draws the top card of the given grade. */
  fun draw(grade: Grade): Int

  /** Converts an integer to the grade of deck it was drawn from. */
  fun Int.getSkillGrade() = when (this) {
    in 1..3 -> Grade.GRADE_GREEN
    in 4..6 -> Grade.GRADE_BLUE
    in 7..9 -> Grade.GRADE_BLACK
    else -> Grade.GRADE_UNSET
  }
}

/** The skill decks available for interaction in the supply. */
object SkillDecksInstance : SkillDecks {
  private val decks: Map<Grade, MutableList<Int>> = mapOf(
    Grade.GRADE_GREEN to mutableListOf(),
    Grade.GRADE_BLUE to mutableListOf(),
    Grade.GRADE_BLACK to mutableListOf(),
  )

  /**
   * Creates a shuffled deck of the given [counts] map, where each key is the card and the value
   * is the number of copies.
   */
  private fun createDeck(counts: Map<Int, Int>): List<Int> {
    return counts
      .map { count -> List(count.value) { count.key } }
      .flatten()
      .shuffled()
  }

  /** Resets the contents of this deck. */
  override fun reset() {
    decks[Grade.GRADE_GREEN]!!.addAll(createDeck(mapOf(1 to 16, 2 to 16, 3 to 16)))
    decks[Grade.GRADE_BLUE]!!.addAll(createDeck(mapOf(4 to 12, 5 to 12, 6 to 12)))
    decks[Grade.GRADE_BLACK]!!.addAll(createDeck(mapOf(7 to 12, 8 to 12, 9 to 12)))
  }

  /** Draws the top card of the deck for the given grade. */
  override fun draw(grade: Grade): Int = decks[grade]!!.removeFirst()
}