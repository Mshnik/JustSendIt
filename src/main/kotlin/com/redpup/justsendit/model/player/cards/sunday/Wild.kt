package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Wild(override val proto: PlayerCardProto) : PlayerCard {
  override fun handleGameEvent(
      event: PlayerGameEvent,
      player: MutablePlayer,
      gameModel: GameModel,
  ) {
    // TODO: PlayerSkiRide event needs to contain the list of revealed cards.
    // Then, check if all are black and add a skill bonus.
    // This will also require a skillBonus property on the player's turn.
  }
}
