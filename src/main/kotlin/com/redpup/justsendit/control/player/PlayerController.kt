package com.redpup.justsendit.control.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.PlayerCard
import com.redpup.justsendit.model.player.proto.TrainingChip

/** Handler for players making decisions. */
interface PlayerController {
  val name: String

  /** Asks the player to choose a player or upgrade card. */
  suspend fun choosePlayerCard(player: Player, cards: List<PlayerCard>): PlayerCard

  /** Asks the player to choose chips to gain from the supply. */
  suspend fun chooseChipsToGain(player: Player, count: Int): List<TrainingChip>

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

  /** Asks the player which chips to use to avoid a crash. */
  suspend fun chooseChipsToUse(
    player: Player,
    tile: SlopeTile,
    currentSkill: Int,
    difficulty: Int,
  ): List<TrainingChip>

  /**
   * Chooses [count] other apres from [otherApres] to apply.
   * If more than one, the order returned is the order applied.
   */
  suspend fun chooseOtherApres(player: Player, otherApres: List<Apres>, count: Int): List<Apres>
}

class BasicPlayerController : PlayerController {
  override val name = "BasicPlayerController-${System.identityHashCode(this)}"

  override suspend fun choosePlayerCard(player: Player, cards: List<PlayerCard>): PlayerCard {
    return cards.first()
  }

  override suspend fun chooseChipsToGain(player: Player, count: Int): List<TrainingChip> {
    TODO("Not yet implemented")
  }

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    TODO("Not yet implemented")
  }

  override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    TODO("Not yet implemented")
  }

  override suspend fun chooseCardsToRemove(
    player: Player,
    cards: List<Int>,
    maxToRemove: Int,
  ): List<Int> {
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

  override suspend fun chooseChipsToUse(
    player: Player,
    tile: SlopeTile,
    currentSkill: Int,
    difficulty: Int,
  ): List<TrainingChip> {
    return emptyList()
  }

  override suspend fun chooseOtherApres(
    player: Player,
    otherApres: List<Apres>,
    count: Int,
  ): List<Apres> {
    TODO()
  }
}