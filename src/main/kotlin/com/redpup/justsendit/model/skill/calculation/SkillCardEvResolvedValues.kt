package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.supply.proto.SkillCardComputationValues
import com.redpup.justsendit.model.supply.proto.SkillCardComputationValuesList
import com.redpup.justsendit.util.TextProtoReaderWriterImpl

// TODO: Make class with resolution.
class SkillCardEvResolvedValues {
  private val parametersList =
    TextProtoReaderWriterImpl<SkillCardComputationValues, SkillCardComputationValuesList.Builder>(
      "src/main/resources/com/redpup/justsendit/model/shop/skill/skill_card_computation_values.textproto",
      SkillCardComputationValuesList::newBuilder,
      SkillCardComputationValuesList.Builder::getValuesList,
      SkillCardComputationValuesList.Builder::addAllValues,
    )

  private var parameters: SkillCardComputationValues.Builder = parametersList().first().toBuilder()

  /** Value of drawing a card. */
  var cardDraw: Double
    get() = parameters.cardDraw;
    set(value) {
      parameters.cardDraw = value
    }

  /** Value of activating an effect again. */
  var reactivate: Double
    get() = parameters.reactivate;
    set(value) {
      parameters.reactivate = value
    }

  /** Value of choosing the best of two cards. */
  var cardFilter2: Double
    get() = parameters.cardFilter2;
    set(value) {
      parameters.cardFilter2 = value
    }

  /** Value of choosing the best of three cards. */
  var cardFilter3: Double
    get() = parameters.cardFilter3;
    set(value) {
      parameters.cardFilter3 = value
    }

  /** Value of trashing a card. */
  var trashCard: Double
    get() = parameters.trashCard;
    set(value) {
      parameters.trashCard = value
    }

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