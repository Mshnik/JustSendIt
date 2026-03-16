package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Rowdy(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: This requires adding a property to MutablePlayer to indicate this ability is active,
    // and updating the bonus calculation logic to use it.
  }

  override fun startTurn() {
    // This is where the flag would be reset.
  }
}
