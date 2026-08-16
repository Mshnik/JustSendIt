package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.ApresGameEvent
import com.redpup.justsendit.model.apres.StockpilingBaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.proto.Die

class Fireworks(override val apresCard: ApresCard) : StockpilingBaseApres(apresCard) {
  override fun handleGameEvent(event: ApresGameEvent, gameModel: GameModel) {
    if (event is ApresGameEvent.PlayerPlayedCard && event.card.diceList.count { it == Die.DIE_BLACK } >= 1) {
      stockpile += 2
    }
  }
}
