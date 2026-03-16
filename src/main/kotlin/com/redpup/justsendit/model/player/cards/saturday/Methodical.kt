package com.redpup.justsendit.model.player.cards.saturday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Methodical(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: Add reorderTopCards to PlayerController
    val topThree = player.skillDeck.take(3)
    // val reordered = player.handler.reorderTopCards(player, topThree)
    // repeat(3) { player.skillDeck.removeFirstOrNull() }
    // player.skillDeck.addAll(0, reordered)
  }
}
