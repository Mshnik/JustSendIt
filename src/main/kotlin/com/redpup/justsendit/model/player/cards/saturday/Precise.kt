package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto
import kotlin.math.max

class Precise(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    player.turn.speed = max(0, player.turn.speed - 2)
  }
}
