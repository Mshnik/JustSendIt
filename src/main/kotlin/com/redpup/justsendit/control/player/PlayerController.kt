package com.redpup.justsendit.control.player

import com.google.common.collect.Range
import com.redpup.justsendit.control.Choice
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.SkiRideResolutionAction

/** Handler for players making decisions. */
interface PlayerController {
  val name: String

  /** Asks the player to some number of a list. */
  suspend fun <T> choose(
    choice: Choice,
    player: Player,
    elements: List<T>,
    count: Range<Int>,
  ): List<T>

  /** Asks the player to one of a list. Defaults to [choose] with input of 1 [count]. */
  suspend fun <T> chooseOne(choice: Choice, player: Player, elements: List<T>): T =
    choose(choice, player, elements, Range.closed(1, 1)).first()

  /** Returns the starting location for a player at the start of a day. */
  suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint

  /** Queues the player to make a mountain decision at the start of their turn. */
  suspend fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision

  /** Asks the player to choose a card to play or stop during ski/ride resolution. */
  suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction
}

class BasicPlayerController : PlayerController {
  override val name = "BasicPlayerController-${System.identityHashCode(this)}"

  override suspend fun <T> choose(
    choice: Choice,
    player: Player,
    elements: List<T>,
    count: Range<Int>,
  ): List<T> {
    return elements.take(count.lowerEndpoint())
  }

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    TODO("Not yet implemented")
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    TODO("Not yet implemented")
  }

  override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    TODO("Not yet implemented")
  }
}