package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Classic(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: This requires adding a property to MutablePlayer to indicate this ability is active,
    // and updating the skill calculation in executeSkiRide to add +2 for blue cards.
  }

  override fun startTurn() {
    // Reset the flag here.
  }
}
