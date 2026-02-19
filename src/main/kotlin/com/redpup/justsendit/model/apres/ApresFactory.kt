package com.redpup.justsendit.model.apres

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

/** Factory for creating [Apres] objects from [ApresCard]s. */
object ApresFactory {
  fun create(apresCard: ApresCard): Apres {
    return when (apresCard.name) {
      "Buy Gear" -> BuyGear(apresCard)
      "Tune-Up" -> TuneUp(apresCard)
      "Study" -> Study(apresCard)
      "First Chair" -> FirstChair(apresCard)
      "Sauna" -> Sauna(apresCard)
      "Bar" -> Bar(apresCard)
      "Dining" -> Dining(apresCard)
      "Village" -> Village(apresCard)
      "Massage" -> Massage(apresCard)
      "Journal" -> Journal(apresCard)
      else -> object : Apres {
        override val apresCard: ApresCard = apresCard
        override fun apply(
          player: MutablePlayer,
          isFirstPlayerToArrive: Boolean,
          gameModel: GameModel,
        ) {
        }
      }
    }
  }
}
