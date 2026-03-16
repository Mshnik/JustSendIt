package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Bold(override val proto: PlayerCardProto) : ActivatedPlayerCard() {

  private var isAbilityActive = false

  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    isAbilityActive = true
  }

  override fun handleGameEvent(
      event: PlayerGameEvent,
      player: MutablePlayer,
      gameModel: GameModel,
  ) {
    if (isAbilityActive && event is PlayerGameEvent.PlayerSkiRide && event.skill < event.difficulty) {
      // TODO: In GameModel, when a crash happens, the points are reset to 0 before this event is sent.
      // This logic needs to be changed to allow this card to keep half the points.
      // For example, the event could carry the points before reset.
      isAbilityActive = false
    }
  }

  override fun startTurn() {
    isAbilityActive = false
  }
}
