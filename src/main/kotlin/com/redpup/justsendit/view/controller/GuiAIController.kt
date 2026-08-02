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
import com.redpup.justsendit.model.player.proto.DieRoll
import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.proto.SkillEffect
import com.redpup.matchers.KMatcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/** A GUI Controller that wraps a [PlayerController] representing CPU control. */
class GuiAIController(
  private val delegate: PlayerController,
  private val delay: Duration = 500.milliseconds,
) : PlayerController {
  override val name: String = delegate.name

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: SkillCardZone,
  ): List<Skill> {
    delay(delay)
    return delegate.chooseSkillCards(gameModel, player, event, elements, count, *zones)
  }

  override suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    delay(delay)
    return delegate.chooseApresCard(gameModel, player, elements, count)
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    delay(delay)
    return delegate.chooseMountainTile(gameModel, player, event, elements)
  }

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    delay(delay)
    return delegate.choosePlayerCard(gameModel, player, elements)
  }

  override suspend fun activateEffects(
    gameModel: GameModel,
    player: Player,
    dice: List<DieRollOrBuilder>,
    effects: List<SkillEffect>,
  ): Boolean {
    delay(delay)
    return delegate.activateEffects(gameModel, player, dice, effects)
  }

  override suspend fun chooseDice(
    gameModel: GameModel,
    player: Player,
    dice: List<DieRoll>,
    matcher: KMatcher<DieRoll>,
    count: Range<Int>,
  ): List<DieRoll> {
    delay(delay)
    return delegate.chooseDice(gameModel, player, dice, matcher, count)
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision {
    delay(delay)
    return delegate.makeMountainDecision(gameModel, player)
  }
}