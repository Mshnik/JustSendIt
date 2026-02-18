package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.proto.MountainDecision

/** Handler for players making decisions. */
interface PlayerHandler {
  /** Queues the player to make a mountain decision. */
  fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision

  /** Returns the starting location for a player at the start of a day. */
  fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint
}

class BasicPlayerHandler : PlayerHandler {
  override fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision {
    TODO("Not yet implemented")
  }

  override fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    TODO("Not yet implemented")
  }
}