package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.supply.proto.PlayerCard as PlayerCardProto
import javax.inject.Inject

interface PlayerFactory {
  /** Factories registered by name. */
  val factories: Map<String, (PlayerCardProto) -> PlayerCard>


  /** Creates an [PlayerCard] from an [PlayerCardProto] using this factory. */
  fun create(card: PlayerCardProto): PlayerCard = factories[card.name]
    ?.let { it(card) }
    ?: throw IllegalArgumentException("No card found for ${card.name} in $factories")
}

/** Factory for creating [Player] objects from [PlayerCard]s. */
class PlayerFactoryImpl @Inject constructor() : PlayerFactory {
  override val factories: Map<String, (PlayerCardProto) -> PlayerCard> = mapOf(
    // TODO
  )
}
