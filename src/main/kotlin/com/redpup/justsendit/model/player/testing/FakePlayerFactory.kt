package com.redpup.justsendit.model.player.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto
import javax.inject.Singleton

/** A testing implementation of [PlayerFactory] */
@VisibleForTesting
@Singleton
class FakePlayerFactory : PlayerFactory {
  override val factories: MutableMap<String, (PlayerCardProto) -> PlayerCard> = mutableMapOf()

  /** Registers [name] to [factory]. This overwrites any previous registration for [name]*/
  fun register(name: String, factory: (PlayerCardProto) -> PlayerCard) {
    factories[name] = factory
  }
}
