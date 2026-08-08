package com.redpup.justsendit.model.skill.calculation

// TODO: Make class with resolution.
object SkillCardEvResolvedValues {
  /** C15 "Card Draw": value of drawing a card. */
  val CARD_DRAW = 4.723558411

  /** C16 "Card Filter (2)": value of replacing the worst of two cards with another. */
  val CARD_FILTER_2 = 0.956443446

  /** C17 "Card Filter (3)": value of replacing the worst of three cards with another. */
  val CARD_FILTER_3 = 1.434665169

  /** C66 "Trash card (Deck/Discard)": value of one point of `GainEffect.trashes`. */
  val TRASH_CARD_DECK_DISCARD = 1.972240652


  /**
   * "Look at the top 3 cards of your deck, put 1 on top, discard the others" combo value
   * (C17). Recognized from three consecutive `card_effect` entries:
   *   TOPDECK -> REVEALED_TOPDECK (count 3),
   *   REVEALED_TOPDECK -> DISCARD (count 2),
   *   REVEALED_TOPDECK -> TOPDECK (count 1).
   */
  val LOOK_AT_TOP_3_KEEP_1 = CARD_FILTER_3

  /**
   * "Draw 2 cards, then put 2 cards on top of your deck" combo value (2 x C16). Recognized
   * from two consecutive `card_effect` entries:
   *   TOPDECK -> HAND (count 2),
   *   HAND -> TOPDECK (count 2).
   */
  val DRAW_2_TOPDECK_2 = 2 * CARD_FILTER_2

  /**
   * Flat value for `filter_hand` ("discard any number of cards, then draw that many"). The
   * sheet's version wasn't scaled by hand size (C16 + C17), and per your direction this stays
   * flat rather than becoming hand-size-aware.
   */
  val FILTER_HAND = CARD_FILTER_2 + CARD_FILTER_3

  /**
   * C4 "Effect EV": AVERAGE(Cards!S2:S1036), the average effect EV across every card in the
   * sheet. Used as the flat value for `reactivate_following` ("activate the effect of the
   * card below an additional time"), per your direction to hardcode this constant.
   */
  val REACTIVATE_FOLLOWING = 2.200542645
}