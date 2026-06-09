package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.justsendit.model.supply.proto.SkillCardList
import com.redpup.justsendit.util.TextProtoReaderImpl
import com.redpup.justsendit.util.pop
import javax.inject.Inject
import javax.inject.Singleton

/** Access to skill decks in the supply. */
interface SkillDecks {
  /** Resets this skill decks to its starting state. */
  fun reset()

  /** Draws the top card of the given grade. */
  fun draw(grade: Grade): SkillCard
}

/** The skill decks available for interaction in the supply. */
@Singleton
class SkillDecksInstance @Inject constructor() : SkillDecks {
  private val decks: Map<Grade, MutableList<SkillCard>> = mapOf(
    Grade.GRADE_GREEN to mutableListOf(),
    Grade.GRADE_BLUE to mutableListOf(),
    Grade.GRADE_BLACK to mutableListOf(),
  )

  private val allCards: List<SkillCard> by lazy {
    TextProtoReaderImpl<SkillCard, SkillCardList.Builder>(
      "src/main/resources/com/redpup/justsendit/model/supply/skill_cards.textproto",
      SkillCardList::newBuilder,
      { cardsList }
    )()
  }

  init {
    reset()
  }

  /** Resets the contents of this deck. */
  override fun reset() {
    decks.values.forEach { it.clear() }
    
    // TODO: Implement Rulebook V2 deck composition.
    // For now, just populate with dummy cards from textproto.
    decks[Grade.GRADE_GREEN]!!.addAll(List(10) { allCards.find { it.name == "Green Starter" }!! })
    decks[Grade.GRADE_BLUE]!!.addAll(List(10) { allCards.find { it.name == "Blue Starter" }!! })
    decks[Grade.GRADE_BLACK]!!.addAll(List(10) { allCards.find { it.name == "Black Starter" }!! })
    
    decks.values.forEach { it.shuffle() }
  }

  /** Draws the top card of the deck for the given grade. */
  override fun draw(grade: Grade): SkillCard =
    decks[grade]!!.pop("$grade deck")
}