package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Nimble(override val proto: PlayerCardProto) : ActivatedPlayerCard() {

  var isAbilityActive = false
    private set

  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    isAbilityActive = true
  }

  // TODO: In GameModel.executeRest, if this ability is active,
  // call a new method on PlayerController to choose a path of up to 3 tiles,
  // and then move the player.

  override fun startTurn() {
    isAbilityActive = false
  }
}
