package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Calm(override val proto: PlayerCardProto) : ActivatedPlayerCard() {

  private var isAbilityActive = false

  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    isAbilityActive = true
  }

  override fun handleGameEvent(
    event: PlayerGameEvent,
    player: MutablePlayer,
    gameModel: GameModel,
  ) {
    // TODO: Add PlayerRested event and broadcast it from GameModel.
    // if (isAbilityActive && event is PlayerGameEvent.PlayerRested) {
    //     player.day.mountainPoints += 10
    //     isAbilityActive = false
    // }
  }

  override fun startTurn() {
    isAbilityActive = false
  }
}
