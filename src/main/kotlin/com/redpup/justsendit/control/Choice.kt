package com.redpup.justsendit.control

/** Represents choices the player can be faced with. */
sealed class Choice {
  data object MapTile : Choice()
  data class SkillCard(val zone: List<SkillCardZone>) : Choice() {
    constructor(vararg zone: SkillCardZone) : this(zone.toList())
  }

  data object PlayerCard : Choice()
  data object ApresCard : Choice()

  companion object {
    val skillCardInHand = SkillCard(SkillCardZone.HAND)
    val skillCardInPlay = SkillCard(SkillCardZone.PLAY)
    val skillCardInDiscard = SkillCard(SkillCardZone.DISCARD)
    val skillCardInDeck = SkillCard(SkillCardZone.DECK)
    val skillCardInShop = SkillCard(SkillCardZone.SHOP)
  }
}

/** Places a SkillCard can be, with regard to a choice. */
enum class SkillCardZone {
  HAND,
  PLAY,
  DISCARD,
  DECK,
  SHOP
}
