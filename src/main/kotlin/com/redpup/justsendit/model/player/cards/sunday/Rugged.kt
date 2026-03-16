package com.redpup.justsendit.model.player.cards.sunday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.ActivatedPlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

class Rugged(override val proto: PlayerCardProto) : ActivatedPlayerCard() {
  override suspend fun onActivate(player: MutablePlayer, gameModel: GameModel) {
    // TODO: This requires a new PlayerController method to choose up to 6 cards from discard.
    // val cardsToShuffle = player.handler.chooseCardsFromDiscard(player, 6)
    // player.skillDiscard.removeAll(cardsToShuffle)
    // player.skillDeck.addAll(cardsToShuffle)
    // player.skillDeck.shuffle()
  }
}
