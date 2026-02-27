package com.redpup.justsendit.control.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision

/** Handler for players making decisions. */
interface PlayerController {
  val name: String

  /** Queues the player to make a mountain decision. */
  suspend fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision

  /** Returns the starting location for a player at the start of a day. */
  suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint

  /** Asks the player to choose cards to remove from their deck. */
  suspend fun chooseCardsToRemove(player: Player, cards: List<Int>, maxToRemove: Int): List<Int>

  /** Asks the player if they want to gain speed. */
  suspend fun shouldGainSpeed(player: Player): Boolean

  /** Ask the player if they want to move when they rest. */
  suspend fun chooseMoveOnRest(player: Player): HexDirection?

  /** Ask the player if they want to use their endurance ability. */
  suspend fun decideToUseEndurance(): Boolean

  /** Show the player the top card of the deck. */
  suspend fun onRevealTopCard(card: Int)
}

class BasicPlayerController : PlayerController {
  override val name = "BasicPlayerController-${System.identityHashCode(this)}"

  override suspend fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision {
    TODO("Not yet implemented")
  }

  override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    TODO("Not yet implemented")
  }

  override suspend fun chooseCardsToRemove(player: Player, cards: List<Int>, maxToRemove: Int): List<Int> {
    // For BasicPlayerHandler, we'll just return an empty list for now.
    // Real implementations would have decision logic.
    return emptyList()
  }

  override suspend fun shouldGainSpeed(player: Player): Boolean {
    // By default, always gain speed.
    return true
  }

  override suspend fun chooseMoveOnRest(player: Player): HexDirection? {
    // By default, don't move on rest.
    return null
  }

  override suspend fun decideToUseEndurance(): Boolean {
    // By default, don't use endurance.
    return false
  }

  override suspend fun onRevealTopCard(card: Int) {
    // By default, do nothing.
  }
}