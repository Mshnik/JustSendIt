package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Steadfast(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: This requires adding a property to MutablePlayer to indicate this ability is active.
    // GameModel.turn() and executeSkiRide() need to be updated to handle the "crash twice in a row" logic.
  }
}
