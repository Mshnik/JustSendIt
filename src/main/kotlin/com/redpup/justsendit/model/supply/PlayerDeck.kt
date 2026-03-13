package com.redpup.justsendit.model.supply

import com.google.inject.Inject
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCardList
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.util.TextProtoReaderImpl
import com.redpup.justsendit.util.pop
import javax.inject.Qualifier
import javax.inject.Singleton

/** Access to the player card deck. */
interface PlayerDeck {
  /** Resets this Apres Deck to its starting state. */
  fun reset()

  /** Draws the top card of the [PlayerCard] deck for the given [day]. */
  fun draw(day: Day): PlayerCard

  /** Draws [count] [PlayerCard]s for the given [day]. */
  fun draw(day: Day, count: Int): MutableList<PlayerCard> {
    val list = mutableListOf<PlayerCard>()
    for (i in 1..count) {
      list.add(draw(day))
    }
    return list
  }
}

/** [Qualifier] for the path of the player deck. */
@Qualifier
annotation class PlayerPath

/** Implementation of [ApresDeck]. */
@Singleton
class PlayerDeckImpl @Inject constructor(
  @PlayerPath path: String,
) :
  PlayerDeck {
  private val reader = TextProtoReaderImpl(
    path,
    PlayerCardList::newBuilder,
    PlayerCardList.Builder::getPlayerList,
    shuffle = true
  )

  /** Reads all cards from [reader]. */
  private fun readCards() = reader().groupBy { it.day }.mapValues { it.value.toMutableList() }

  var cards = readCards(); private set

  /** Returns the list of cards for testing. */
  internal fun getCards(): Map<Day, List<PlayerCard>> = cards

  /** Draws the top card from the Player deck. */
  override fun draw(day: Day): PlayerCard = cards[day]!!.pop("Player deck")

  /** Resets the Apres deck. */
  override fun reset() {
    cards = readCards()
  }
}