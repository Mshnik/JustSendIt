package com.redpup.justsendit.model.player.cards.friday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class James(override val proto: PlayerCardProto) : PlayerCard {
  override fun handleGameEvent(
    event: PlayerGameEvent,
    player: MutablePlayer,
    gameModel: GameModel,
  ) {
    if (event is PlayerGameEvent.PlayerSkiRide && event.slope.hazardsList.isEmpty()) {
      player.turn.points += 1
    }
  }
}
