package com.redpup.justsendit.view.controller

import com.google.common.collect.Range
import com.redpup.justsendit.control.MountainTileEvent
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.control.SkillEvent
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill
import kotlinx.coroutines.delay

/** A GUI Controller that wraps a [PlayerController] representing CPU control. */
class GuiAIController(private val delegate: PlayerController) : PlayerController {
  override val name: String = delegate.name

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    delay(500)
    return delegate.chooseSkillCards(gameModel, player, event, elements, count, *zones)
  }

  override suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    delay(500)
    return delegate.chooseApresCard(gameModel, player, elements, count)
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    delay(500)
    return delegate.chooseMountainTile(gameModel, player, event, elements)
  }

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    delay(500)
    return delegate.choosePlayerCard(gameModel, player, elements)
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision {
    delay(500)
    return delegate.makeMountainDecision(gameModel, player)
  }
}