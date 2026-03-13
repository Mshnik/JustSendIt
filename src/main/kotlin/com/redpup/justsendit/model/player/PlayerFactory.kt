package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.proto.PlayerCard
import javax.inject.Inject

interface PlayerFactory {
  /** Creates a [Player] from a [PlayerCard] using this factory. */
  fun create(handler: PlayerController): MutablePlayer
}

/** Factory for creating [Player] objects from [PlayerCard]s. */
class PlayerFactoryImpl @Inject constructor() : PlayerFactory {
  /** Creates a [Player] from a [PlayerCard] using this factory. */
  override fun create(handler: PlayerController): MutablePlayer {
    return MutablePlayer(handler)
  }
}
