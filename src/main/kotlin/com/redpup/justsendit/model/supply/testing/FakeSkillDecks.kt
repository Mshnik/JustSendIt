package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.util.pop
import javax.inject.Inject
import javax.inject.Singleton

/** A testing fake implementation of [SkillDecks]. */
@VisibleForTesting
@Singleton
class FakeSkillDecks @Inject constructor() : SkillDecks {
  private val greenDeck: MutableList<SkillCard> = mutableListOf()
  private val blueDeck: MutableList<SkillCard> = mutableListOf()
  private val blackDeck: MutableList<SkillCard> = mutableListOf()

  /** Sets [greenDeck] to the given input, overriding any existing input. */
  fun setGreenDeck(cards: List<SkillCard>) {
    greenDeck.clear()
    greenDeck.addAll(cards)
  }

  /** Sets [blueDeck] to the given input, overriding any existing input. */
  fun setBlueDeck(cards: List<SkillCard>) {
    blueDeck.clear()
    blueDeck.addAll(cards)
  }

  /** Sets [blackDeck] to the given input, overriding any existing input. */
  fun setBlackDeck(cards: List<SkillCard>) {
    blackDeck.clear()
    blackDeck.addAll(cards)
  }

  override fun reset() {
    greenDeck.clear()
    blueDeck.clear()
    blackDeck.clear()
  }

  override fun draw(grade: Grade): SkillCard {
    return when (grade) {
      Grade.GRADE_GREEN -> greenDeck.pop("Green deck")
      Grade.GRADE_BLUE -> blueDeck.pop("Blue deck")
      Grade.GRADE_BLACK -> blackDeck.pop("Black deck")
      else -> throw IllegalArgumentException("Invalid grade for drawing skill card: $grade")
    }
  }
}
