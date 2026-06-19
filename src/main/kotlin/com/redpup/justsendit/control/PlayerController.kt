package com.redpup.justsendit.control

import com.google.common.collect.Range
import com.redpup.justsendit.control.PlayerController.SkillZone
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
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
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: SkillZone,
  ): List<Skill>

  /** Asks the player to choose some number of Apres cards from a list. */
  suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres>

  /** Asks the player to choose a mountain tile from a list. */
  suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint

  /** Asks the player to choose a player card from a list. */
  suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard

  /** Queues the player to make a mountain decision at the start of their turn. */
  suspend fun makeMountainDecision(gameModel: GameModel, player: Player): MountainDecision
}

class BasicPlayerController : PlayerController {
  override val name = "BasicPlayerController-${System.identityHashCode(this)}"
  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: SkillZone,
  ): List<Skill> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    TODO("Not yet implemented")
  }

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    TODO("Not yet implemented")
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision {
    TODO("Not yet implemented")
  }
}