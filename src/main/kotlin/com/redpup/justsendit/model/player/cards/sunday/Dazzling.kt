package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Dazzling(override val proto: PlayerCardProto) : PlayerCard {
  override fun startDay() {
    // TODO: At the start of Sunday, set aside 3 cards face up.
    // This requires adding a property to MutablePlayer to store these cards.
    // And a way for the player to choose them.
  }

  // TODO: The logic for "when you draw [card], you can play a face up"
  // needs to be implemented. This requires modifying the game's card drawing logic.
}
