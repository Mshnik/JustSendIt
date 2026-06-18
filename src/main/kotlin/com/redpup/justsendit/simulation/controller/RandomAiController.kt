package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.passDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.SkiRideResolutionAction
import com.redpup.justsendit.model.player.proto.SkiRideResolutionActionKt.playCardAction
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.skiRideResolutionAction
import com.redpup.justsendit.model.skill.Skill

/** An AI controller that makes random choices. */
class RandomAiController(override val name: String) : PlayerController {

  override suspend fun chooseSkillCards(
    player: Player,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return elements.shuffled()
      .take(count.lowerEndpoint() + (0..count.upperEndpoint() - count.lowerEndpoint()).random())
  }

  override suspend fun chooseApresCard(
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    return elements.shuffled()
      .take(count.lowerEndpoint() + (0..count.upperEndpoint() - count.lowerEndpoint()).random())
  }

  override suspend fun chooseMountainTile(
    player: Player,
    elements: Collection<HexPoint>,
  ): HexPoint {
    return elements.shuffled().first()
  }

  override suspend fun choosePlayerCard(player: Player, elements: List<PlayerCard>): PlayerCard {
    return elements.random()
  }

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    val moves = gameModel.getAvailableMoves(player)
    val location = player.location
    val tile = location?.let { gameModel.tileMap[it] }

    val options = mutableListOf<MountainDecision>()

    // Always can pass
    options.add(mountainDecision { pass = passDecision {} })

    if (moves.isNotEmpty()) {
      options.add(mountainDecision {
        skiRide = skiRideDecision {
          direction = moves.values.random()
        }
      })
    }

    if (tile?.hasLift() == true && player.hand.size >= tile.lift.minCards) {
      options.add(mountainDecision { lift = MountainDecision.LiftDecision.getDefaultInstance() })
    }

    if (tile?.apresLink ?: 0 > 0) {
      options.add(mountainDecision { exit = MountainDecision.ExitDecision.getDefaultInstance() })
    }

    return options.random()
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    return if (player.hand.isNotEmpty()) {
      skiRideResolutionAction { play = playCardAction { cardName = player.hand.random().name } }
    } else {
      skiRideResolutionAction { stop = empty {} }
    }
  }
}
