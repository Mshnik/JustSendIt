package com.redpup.justsendit.model.apres.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.proto.ApresCard

/** A testing implementation of [ApresFactory] */
@VisibleForTesting
class FakeApresFactory : ApresFactory {
  override val factories: MutableMap<String, (ApresCard) -> Apres> = mutableMapOf()

  /** Registers [name] to [factory]. This overwrites any previous registration for [name]*/
  fun register(name: String, factory: (ApresCard) -> Apres) {
    factories[name] = factory
  }
}