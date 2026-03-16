package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Determined(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    player.skillDeck.addAll(player.skillDiscard)
    player.skillDeck.shuffle()
    player.skillDiscard.clear()
  }
}
