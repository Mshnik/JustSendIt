package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.friday.*
import com.redpup.justsendit.model.player.cards.saturday.*
import com.redpup.justsendit.model.player.cards.sunday.*
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto
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
    "George" to { George(it) },
    "Jenny" to { Jenny(it) },
    "Michael" to { Michael(it) },
    "Andy" to { Andy(it) },
    "James" to { James(it) },
    "David" to { David(it) },
    "Dannver" to { Dannver(it) },
    "Yifei" to { Yifei(it) },
    "Courtney" to { Courtney(it) },

    "Reckless" to { Reckless(it) },
    "Methodical" to { Methodical(it) },
    "Swift" to { Swift(it) },
    "Calm" to { Calm(it) },
    "Precise" to { Precise(it) },
    "Relentless" to { Relentless(it) },
    "Bold" to { Bold(it) },
    "Rowdy" to { Rowdy(it) },
    "Determined" to { Determined(it) },

    "Wild" to { Wild(it) },
    "Tough" to { Tough(it) },
    "Steadfast" to { Steadfast(it) },
    "Nimble" to { Nimble(it) },
    "Fluid" to { Fluid(it) },
    "Rigorous" to { Rigorous(it) },
    "Classic" to { Classic(it) },
    "Rugged" to { Rugged(it) },
    "Dazzling" to { Dazzling(it) },
  )
}
