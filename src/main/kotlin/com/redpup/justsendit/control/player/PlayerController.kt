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

  /** Places a Skill can be, with regard to a choice. */
  enum class SkillZone {
    HAND,
    PLAY,
    DISCARD,
    DECK,
    SHOP
  }

  /** Asks the player to choose some number of skill cards from a list. */
  suspend fun chooseSkillCards(
    player: Player,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: SkillZone,
  ): List<Skill>

  /** Asks the player to choose some number of Apres cards from a list. */
  suspend fun chooseApresCard(
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres>

  /** Asks the player to choose a mountain tile from a list. */
  suspend fun chooseMountainTile(
    player: Player,
    elements: List<HexPoint>,
  ): HexPoint

  /** Asks the player to choose a player card from a list. */
  suspend fun choosePlayerCard(
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard

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
  override suspend fun chooseSkillCards(
    player: Player,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseApresCard(
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseMountainTile(player: Player, elements: List<HexPoint>): HexPoint {
    TODO("Not yet implemented")
  }

  override suspend fun choosePlayerCard(player: Player, elements: List<PlayerCard>): PlayerCard {
    TODO("Not yet implemented")
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
}