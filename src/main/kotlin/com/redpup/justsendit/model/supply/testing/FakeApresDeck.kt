package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.util.pop
import javax.inject.Inject
import javax.inject.Singleton

/** A testing fake implementation of [ApresDeck]. */
@VisibleForTesting
@Singleton
class FakeApresDeck @Inject constructor(
  private val factory: ApresFactory,
) : ApresDeck {
  private var apresCards: MutableList<ApresCard> = mutableListOf()

  /** Adds all [cards] to [apresCards]. */
  fun add(vararg cards: ApresCard) {
    this.apresCards.addAll(cards)
  }

  override fun reset() {
    apresCards.clear()
  }

  override fun draw(): Apres {
    return factory.create(apresCards.pop("Apres Deck"))
  }

  override fun tuck(apres: Apres) {
    apresCards.addLast(apres.apresCard)
  }
}
