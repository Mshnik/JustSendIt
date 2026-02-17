package com.redpup.justsendit.model.supply.testing

import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks

/** A testing fake implementation of [SkillDecks]. */
class FakeSkillDecks : SkillDecks {
    private val greenDeck: MutableList<Int> = mutableListOf()
    private val blueDeck: MutableList<Int> = mutableListOf()
    private val blackDeck: MutableList<Int> = mutableListOf()

    /** Sets [greenDeck] to the given input, overriding any existing input. */
    fun setGreenDeck(cards: List<Int>) {
        greenDeck.clear()
        greenDeck.addAll(cards)
    }

    /** Sets [blueDeck] to the given input, overriding any existing input. */
    fun setBlueDeck(cards: List<Int>) {
        blueDeck.clear()
        blueDeck.addAll(cards)
    }

    /** Sets [blackDeck] to the given input, overriding any existing input. */
    fun setBlackDeck(cards: List<Int>) {
        blackDeck.clear()
        blackDeck.addAll(cards)
    }

    override fun reset() {
        greenDeck.clear()
        blueDeck.clear()
        blackDeck.clear()
    }

    override fun draw(grade: Grade): Int {
        return when (grade) {
            Grade.GRADE_GREEN -> greenDeck.removeFirst()
            Grade.GRADE_BLUE -> blueDeck.removeFirst()
            Grade.GRADE_BLACK -> blackDeck.removeFirst()
            else -> throw IllegalArgumentException("Invalid grade for drawing skill card: $grade")
        }
    }
}
