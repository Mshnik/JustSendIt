package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Relentless(override val proto: PlayerCardProto) : ActivatedPlayerCard() {

  var isAbilityActive = false
    private set

  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    isAbilityActive = true
  }

  // TODO: In GameModel, after a crash, check if this card's ability is active.
  // If so, don't advance currentPlayerIndex and start a new turn for the same player.
  // The flag should be reset after the extra turn.

  override fun startDay() {
    isAbilityActive = false
  }
}
