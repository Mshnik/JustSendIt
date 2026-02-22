package com.redpup.justsendit.model.apres

import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.ApresCard
import javax.inject.Inject

interface ApresFactory {
  /** Factories registered by name. */
  val factories: Map<String, (ApresCard) -> Apres>

  /** Creates an [Apres] from an [ApresCard] using this factory. */
  fun create(apresCard: ApresCard): Apres = factories[apresCard.name]
    ?.let { it(apresCard) }
    ?: throw IllegalArgumentException("No card found for ${apresCard.name} in $factories")
}

/** Factory for creating [Apres] objects from [ApresCard]s. */
class ApresFactoryImpl @Inject constructor() : ApresFactory {
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
