package com.redpup.justsendit.model.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.proto.MountainDecision

/** Handler for players making decisions. */
interface PlayerHandler {
  /** Queues the player to make a mountain decision. */
  fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision
}