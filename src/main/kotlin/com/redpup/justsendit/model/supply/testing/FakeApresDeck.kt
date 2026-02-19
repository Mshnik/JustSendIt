package com.redpup.justsendit.model.supply.testing

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.ApresDeckImpl

/** A fake testing implementation of [ApresDeck] */
class FakeApresDeck(path: String) : ApresDeckImpl(path) {
  private val factory = FakeApresFactory()
  private val values = mutableMapOf<String, Apres>()

  /**
   * Adds [name] pointing to [apres] in this fake deck.
   * Overwrites any previous registration for [name]
   */
  fun register(name: String, apres: Apres) {
    values[name] = apres
    factory.factories[name] = { _ -> apres }
  }

  /** Returns the [Apres] registered for [name]. */
  operator fun get(name: String): Apres {
    return values[name]!!
  }

  override fun draw(): Apres = factory.create(cards.removeFirst())
}