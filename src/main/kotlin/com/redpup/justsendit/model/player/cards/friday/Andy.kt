package com.redpup.justsendit.model.player.cards.friday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Andy(override val proto: PlayerCardProto) : PlayerCard {
  override fun handleGameEvent(
    event: PlayerGameEvent,
    player: MutablePlayer,
    gameModel: GameModel,
  ) {
    if (event is PlayerGameEvent.PlayerSkiRide && event.skill >= event.difficulty) {
      // TODO: Fix this timing so it has to happen when you choose to continue to ski/ride
      // not after a ski/ride finishes.
      player.turn.points += player.turn.speed
    }
  }
}
