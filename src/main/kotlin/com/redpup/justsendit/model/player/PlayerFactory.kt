package com.redpup.justsendit.model.player

import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.player.cards.*
import com.redpup.justsendit.model.player.proto.PlayerCard
import javax.inject.Inject

interface PlayerFactory {
  /** Creates a [Player] from a [PlayerCard] using this factory. */
  fun create(playerCard: PlayerCard, handler: PlayerController): MutablePlayer
}

/** Factory for creating [Player] objects from [PlayerCard]s. */
class PlayerFactoryImpl @Inject constructor() : PlayerFactory {
  private val factories: Map<String, (PlayerCard, PlayerController) -> MutablePlayer> = mapOf(
    "Amy" to ::Amy.createPlayerFunctor(),
    "Andy" to ::Andy.createPlayerFunctor(),
    "Courtney" to ::Courtney.createPlayerFunctor(),
    "Dannver" to ::Dannver.createPlayerFunctor(),
    "David" to ::David.createPlayerFunctor(),
    "George" to ::George.createPlayerFunctor(),
    "James" to ::James.createPlayerFunctor(),
    "Jenny" to ::Jenny.createPlayerFunctor(),
    "Mia" to ::Mia.createPlayerFunctor(),
    "Michael" to ::Michael.createPlayerFunctor(),
    "Yifei" to ::Yifei.createPlayerFunctor(),
    "Melissa" to ::Melissa.createPlayerFunctor(),
    "Wendy" to ::Wendy.createPlayerFunctor(),
    "Test 1" to ::Test1.createPlayerFunctor(),
    "Test 2" to ::Test2.createPlayerFunctor(),
    "Test 3" to ::Test3.createPlayerFunctor(),
    "Test 4" to ::Test4.createPlayerFunctor()
  )

  private fun ((Player) -> AbilityHandler).createPlayerFunctor(): (PlayerCard, PlayerController) -> MutablePlayer {
    return { playerCard, handler -> MutablePlayer(playerCard, handler, this@createPlayerFunctor) }
  }

  /** Creates a [Player] from a [PlayerCard] using this factory. */
  override fun create(playerCard: PlayerCard, handler: PlayerController): MutablePlayer =
    factories[playerCard.name]
      ?.let { it(playerCard, handler) }
      ?: throw IllegalArgumentException("No card found for ${playerCard.name} in $factories")
}
