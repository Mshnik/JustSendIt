package com.redpup.justsendit.model.player.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.player.*
import com.redpup.justsendit.model.player.proto.PlayerCard

/** A testing implementation of [PlayerFactory] */
@VisibleForTesting
class FakePlayerFactory : PlayerFactory {
  private val abilityHandlers = mutableMapOf<String, AbilityHandler>()

  /** Registers [name] to [factory]. This overwrites any previous registration for [name]*/
  fun register(name: String, abilityHandler: AbilityHandler) {
    abilityHandlers[name] = abilityHandler
  }

  /** Creates a [Player] from a [PlayerCard] using this factory. */
  override fun create(playerCard: PlayerCard, handler: PlayerHandler): MutablePlayer =
    MutablePlayer(playerCard, handler) { _ -> abilityHandlers[playerCard.name]!! }
}
