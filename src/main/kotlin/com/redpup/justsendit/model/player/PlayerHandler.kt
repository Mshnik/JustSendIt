package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.proto.MountainDecision

/** Handler for players making decisions. */
interface PlayerHandler {
  /** Queues the player to make a mountain decision. */
  fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision

  /** Returns the starting location for a player at the start of a day. */
  fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint

  /** Asks the player to choose cards to remove from their deck. */
  fun chooseCardsToRemove(player: Player, cards: List<Int>, maxToRemove: Int): List<Int>
  /** Asks the player if they want to gain speed. */
  fun shouldGainSpeed(player: Player): Boolean
  fun chooseMoveOnRest(player: Player): HexDirection?
}

class BasicPlayerHandler : PlayerHandler {
  override fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision {
    TODO("Not yet implemented")
  }

  override fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    TODO("Not yet implemented")
  }

  override fun chooseCardsToRemove(player: Player, cards: List<Int>, maxToRemove: Int): List<Int> {
    // For BasicPlayerHandler, we'll just return an empty list for now.
    // Real implementations would have decision logic.
    return emptyList()
  }

  override fun shouldGainSpeed(player: Player): Boolean {
    // By default, always gain speed.
    return true
  }

  override fun chooseMoveOnRest(player: Player): HexDirection? {
    // By default, don't move on rest.
    return null
  }
}