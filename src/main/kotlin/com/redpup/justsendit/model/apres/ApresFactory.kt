package com.redpup.justsendit.model.apres

import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.ApresCard

interface ApresFactory {
  /** Factories registered by name. */
  val factories: Map<String, (ApresCard) -> Apres>

  /** Creates an [Apres] from an [ApresCard] using this factory. */
  fun create(apresCard: ApresCard): Apres = factories[apresCard.name]!!(apresCard)
}

/** Factory for creating [Apres] objects from [ApresCard]s. */
object ApresFactoryImpl : ApresFactory {
  override val factories: Map<String, (ApresCard) -> Apres> = mapOf(
    "Buy Gear" to { apresCard -> BuyGear(apresCard) },
    "Tune-Up" to { apresCard -> TuneUp(apresCard) },
    "Study" to { apresCard -> Study(apresCard) },
    "First Chair" to { apresCard -> FirstChair(apresCard) },
    "Sauna" to { apresCard -> Sauna(apresCard) },
    "Bar" to { apresCard -> Bar(apresCard) },
    "Dining" to { apresCard -> Dining(apresCard) },
    "Village" to { apresCard -> Village(apresCard) },
    "Massage" to { apresCard -> Massage(apresCard) },
    "Journal" to { apresCard -> Journal(apresCard) },
  )
}
