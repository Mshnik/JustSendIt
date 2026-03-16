package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Tough(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: This ability needs to be triggered after revealing cards in a ski/ride.
    // It should discard the revealed cards and draw new ones.
    // This requires a new event or a change in the game flow.
  }
}
