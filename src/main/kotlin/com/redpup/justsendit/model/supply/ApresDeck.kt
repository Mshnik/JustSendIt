package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.ApresFactoryImpl
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.apres.proto.ApresCardList
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Access to the apres card deck. */
interface ApresDeck {
  /** Resets this Apres Deck to its starting state. */
  fun reset()

  /** Draws the top card of the [Apres] deck. */
  fun draw(): Apres

  /** Tucks the given card under the deck. */
  fun tuck(apres: Apres)

  /** Draws a card for the given day. Draws until a card for the given day is found. */
  fun drawForDay(day: Int): Apres {
    // for instead of while is safety for infinite looping.
    for (i in 0..100) {
      val apres = draw()
      if (day in apres.apresCard.availableDaysList) {
        return apres
      }
      tuck(apres)
    }
    throw IllegalArgumentException("No cards found for day $day")
  }
}

/** Implementation of [ApresDeck]. */
class ApresDeckImpl(path: String, val factory: ApresFactory = ApresFactoryImpl) :
  ApresDeck {
  private val reader = TextProtoReaderImpl(
    path,
    ApresCardList::newBuilder,
    ApresCardList.Builder::getApresList,
    shuffle = true
  )
  val cards = reader().toMutableList()

  /** Returns the list of cards for testing. */
  internal fun getCards(): List<ApresCard> = cards.toList()

  /** Draws the top card from the Apres deck. */
  override fun draw() = factory.create(cards.removeFirst())

  /** Tucks the given card under the apres deck. */
  override fun tuck(apres: Apres) {
    cards.add(apres.apresCard)
  }

  /** Resets the Apres deck. */
  override fun reset() {
    cards.clear()
    cards.addAll(reader())
  }
}