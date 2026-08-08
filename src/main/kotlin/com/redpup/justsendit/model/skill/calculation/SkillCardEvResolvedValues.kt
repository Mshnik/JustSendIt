package com.redpup.justsendit.model.skill.calculation

// TODO: Make class with resolution.
class SkillCardEvResolvedValues {
  /** C15 "Card Draw": value of drawing a card. */
  var cardDraw = 4.723558411

  /** C16 "Card Filter (2)": value of replacing the worst of two cards with another. */
  var cardFilter2 = 0.956443446

  /** C17 "Card Filter (3)": value of replacing the worst of three cards with another. */
  var cardFilter3 = 1.434665169

  /** C66 "Trash card (Deck/Discard)": value of one point of `GainEffect.trashes`. */
  var trashCard = 1.972240652

  /**
   * C4 "Effect EV": AVERAGE(Cards!S2:S1036), the average effect EV across every card in the
   * sheet. Used as the flat value for `reactivate_following` ("activate the effect of the
   * card below an additional time"), per your direction to hardcode this constant.
   */
  var reactivateFollowing = 2.200542645

  /**
   * "Look at the top 3 cards of your deck, put 1 on top, discard the others" combo value
   * (C17). Recognized from three consecutive `card_effect` entries:
   *   TOPDECK -> REVEALED_TOPDECK (count 3),
   *   REVEALED_TOPDECK -> DISCARD (count 2),
   *   REVEALED_TOPDECK -> TOPDECK (count 1).
   */
  val lookAtTop3Keep1: Double get() = cardFilter3

  /**
   * "Draw 2 cards, then put 2 cards on top of your deck" combo value (2 x C16). Recognized
   * from two consecutive `card_effect` entries:
   *   TOPDECK -> HAND (count 2),
   *   HAND -> TOPDECK (count 2).
   */
  val draw2Topdeck2: Double get() = 2 * cardFilter2

  /**
   * Flat value for `filter_hand` ("discard any number of cards, then draw that many"). The
   * sheet's version wasn't scaled by hand size (C16 + C17), and per your direction this stays
   * flat rather than becoming hand-size-aware.
   */
  val filterHand: Double get() = cardFilter2 + cardFilter3
}