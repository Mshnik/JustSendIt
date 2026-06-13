package com.redpup.justsendit.control.player

import com.google.common.collect.Range
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.SkiRideResolutionAction
import com.redpup.justsendit.model.skill.Skill

/** Handler for players making decisions. */
interface PlayerController {
  val name: String

  /** Asks the player to choose a player or upgrade card. */
  suspend fun choosePlayerCard(player: Player, cards: List<PlayerCard>): PlayerCard

  /** Queues the player to make a mountain decision. */
  suspend fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision

  /** Returns the starting location for a player at the start of a day. */
  suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint

  /** Asks the player to choose cards to remove from their deck. */
  suspend fun chooseCardsToRemove(
    player: Player,
    cards: List<Skill>,
    maxToRemove: Int,
  ): List<Skill>

  /**
   * Chooses [count] other apres from [otherApres] to apply.
   * If more than one, the order returned is the order applied.
   */
  suspend fun chooseOtherApres(player: Player, otherApres: List<Apres>, count: Int): List<Apres>

  /** Asks the player to choose a card to play or stop during ski/ride resolution. */
  suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction

  /** Asks the player to choose cards to discard from hand. */
  suspend fun chooseCardsToDiscard(
    player: Player,
    hand: List<Skill>,
    count: Range<Int>,
  ): List<Skill>

  /** Asks the player to choose cards to trash. */
  suspend fun chooseCardsToTrash(
    player: Player,
    candidates: List<Skill>,
    count: Range<Int>,
  ): List<Skill>

  /** Asks the player to choose cards from their discard pile to retrieve (e.g. for Rugged card). */
  suspend fun chooseCardsFromDiscard(
    player: Player,
    discard: List<Skill>,
    count: Range<Int>,
  ): List<Skill>
}

class BasicPlayerController : PlayerController {
  override val name = "BasicPlayerController-${System.identityHashCode(this)}"

  override suspend fun choosePlayerCard(player: Player, cards: List<PlayerCard>): PlayerCard {
    return cards.first()
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
    cards: List<Skill>,
    maxToRemove: Int,
  ): List<Skill> {
    return emptyList()
  }

  override suspend fun chooseOtherApres(
    player: Player,
    otherApres: List<Apres>,
    count: Int,
  ): List<Apres> {
    return otherApres.take(count)
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    TODO("Not yet implemented")
  }

  override suspend fun chooseCardsToDiscard(
    player: Player,
    hand: List<Skill>,
    count: Range<Int>,
  ): List<Skill> {
    return hand.take(count.upperEndpoint())
  }

  override suspend fun chooseCardsToTrash(
    player: Player,
    candidates: List<Skill>,
    count: Range<Int>,
  ): List<Skill> {
    return emptyList()
  }

  override suspend fun chooseCardsFromDiscard(
    player: Player,
    discard: List<Skill>,
    count: Range<Int>,
  ): List<Skill> {
    return discard.take(count.upperEndpoint())
  }
}