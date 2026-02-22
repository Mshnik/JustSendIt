package com.redpup.justsendit.model.supply

import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.player.proto.PlayerCardList
import com.redpup.justsendit.util.TextProtoReaderImpl
import com.google.inject.Inject
import com.redpup.justsendit.util.pop
import javax.inject.Qualifier
import javax.inject.Singleton

/** Access to the player card deck. */
interface PlayerDeck {
  /** Resets this Apres Deck to its starting state. */
  fun reset()

  /** Draws the top card of the [PlayerCard] deck. */
  fun draw(): PlayerCard
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
  val cards = reader().toMutableList()

  /** Returns the list of cards for testing. */
  internal fun getCards(): List<PlayerCard> = cards.toList()

  /** Draws the top card from the Player deck. */
  override fun draw(): PlayerCard = cards.pop("Player deck")

  /** Resets the Apres deck. */
  override fun reset() {
    cards.clear()
    cards.addAll(reader())
  }
}