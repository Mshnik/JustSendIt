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
    "Bar" to { Bar(it) },
    "Concert" to { Concert(it) },
    "Dining" to { Dining(it) },
    "Dog Sledding" to { DogSledding(it) },
    "Fire Pit" to { FirePit(it) },
    "Fireworks" to { Fireworks(it) },
    "Ice Skating" to { IceSkating(it) },
    "Karaoke" to { Karaoke(it) },
    "Lodge" to { Lodge(it) },
    "Massage" to { Massage(it) },
    "Photo-Op" to { PhotoOp(it) },
    "Sauna" to { Sauna(it) },
  )
}
