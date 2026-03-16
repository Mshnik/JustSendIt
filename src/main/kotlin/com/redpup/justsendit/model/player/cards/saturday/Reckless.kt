package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Reckless(override val proto: PlayerCardProto) : ActivatedPlayerCard() {

  private var isAbilityActive = false

  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    isAbilityActive = true
    // TODO: Implement revealing one additional card.
  }

  override fun handleGameEvent(
      event: PlayerGameEvent,
      player: MutablePlayer,
      gameModel: GameModel,
  ) {
    if (isAbilityActive && event is PlayerGameEvent.PlayerSkiRide && event.skill < event.difficulty) {
      player.turn.points -= 5
    }
  }

  override fun startTurn() {
    isAbilityActive = false
  }
}
