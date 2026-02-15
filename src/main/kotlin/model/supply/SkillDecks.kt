package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.board.tile.proto.Grade

/** The skill decks available for interaction in the supply. */
class SkillDecks {
  private val decks: Map<Grade, MutableList<Int>> = buildMap {
    put(Grade.GRADE_GREEN, createDeck(mapOf(1 to 16, 2 to 16, 3 to 16)))
    put(Grade.GRADE_BLUE, createDeck(mapOf(4 to 12, 5 to 12, 6 to 12)))
    put(Grade.GRADE_BLACK, createDeck(mapOf(7 to 12, 8 to 12, 9 to 12)))
  }

  /**
   * Creates a shuffled deck of the given [counts] map, where each key is the card and the value
   * is the number of copies.
   */
  private fun createDeck(counts: Map<Int, Int>): MutableList<Int> {
    return counts
      .map { count -> List(count.value) { count.key } }
      .flatten()
      .shuffled()
      .toMutableList()
  }

  /** Draws the top card of the deck for the given grade. */
  fun draw(grade: Grade): Int = decks[grade]!!.removeFirst()
}