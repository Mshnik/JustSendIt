package com.redpup.justsendit.model.apres.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.ApresGameEvent
import com.redpup.justsendit.model.apres.StockpilingBaseApres
import com.redpup.justsendit.model.apres.proto.ApresCard

class DogSledding(override val apresCard: ApresCard) : StockpilingBaseApres(apresCard) {
  override fun handleGameEvent(event: ApresGameEvent, gameModel: GameModel) {
    if (event is ApresGameEvent.PlayerSkiRide) {
      if (event.turn == 1 && event.success) {
        stockpile += 4
      }
    }
  }
}
