package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.grid.HexExtensions.plus
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Icons.matches
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
import com.redpup.justsendit.model.supply.proto.SkillCard

/** A simple AI player controller for simulation. */
class SimpleAiController(override val name: String) : PlayerController {

  override suspend fun chooseSkillCards(
    player: Player,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    // Just pick the first N elements.
    return elements.take(count.upperEndpoint())
  }

  override suspend fun chooseApresCard(
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    return elements.take(count.upperEndpoint())
  }

  override suspend fun chooseMountainTile(
    player: Player,
    elements: Collection<HexPoint>,
  ): HexPoint {
    return elements.first()
  }

  override suspend fun choosePlayerCard(player: Player, elements: List<PlayerCard>): PlayerCard {
    return elements.first()
  }

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    val location = player.location ?: return mountainDecision { pass = passDecision { } }
    val tile = gameModel.tileMap[location] ?: return mountainDecision { pass = passDecision { } }

    // 1. If on an exit tile, EXIT.
    if (tile.apresLink > 0) {
      return mountainDecision { exit = MountainDecision.ExitDecision.getDefaultInstance() }
    }

    // 2. If on a lift tile and has cards to discard, LIFT.
    if (tile.hasLift() && player.hand.size >= tile.lift.minCards) {
      return mountainDecision { lift = MountainDecision.LiftDecision.getDefaultInstance() }
    }

    // 3. If there are available moves down, SKI_RIDE.
    val moves = gameModel.getAvailableMoves(player)
    if (moves.isNotEmpty()) {
      // For now, just pick the first move.
      return mountainDecision { 
        skiRide = skiRideDecision { 
          direction = moves.values.first() 
        } 
      }
    }

    // 4. Otherwise, PASS.
    return mountainDecision { 
      pass = passDecision { 
        // Try to buy a card if possible.
        val studyValue = calculateStudyValue(player, gameModel)
        val affordable = gameModel.shop.entries
          .filter { (it.key.skillCard.cost - it.value).coerceAtLeast(0) <= studyValue }
          .sortedByDescending { it.key.skillCard.cost }
        
        if (affordable.isNotEmpty()) {
          buyCardName = affordable.first().key.name
        }
      } 
    }
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    // Pick the card with the highest expected skill value.
    val location = player.location!!
    val tile = gameModel.tileMap[location]!!
    val slope = tile.slope

    val bestCard = player.hand.maxByOrNull { calculateExpectedValue(it.skillCard, slope) }
    return if (bestCard != null) {
      skiRideResolutionAction { play = playCardAction { cardName = bestCard.name } }
    } else {
      // This shouldn't happen as GameModel handles empty hand, but for safety:
      skiRideResolutionAction { stop = empty {} }
    }
  }

  private fun calculateExpectedValue(card: SkillCard, slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile): Double {
    val diceValue = (card.greenDice * 3.5) + (card.blueDice * 3.5) + (card.blackDice * 3.5)
    val iconValue = card.iconsList.count { it.matches(slope) }.toDouble()
    return diceValue + iconValue
  }

  private fun calculateStudyValue(player: Player, gameModel: GameModel): Int {
    var total = player.hand.size
    val currentLocation = player.location ?: return total
    val tile = gameModel.tileMap[currentLocation] ?: return total

    if (tile.hasSlope()) {
      val slope = tile.slope
      for (skill in player.hand) {
        total += skill.skillCard.iconsList.count { it.matches(slope) }
      }
    } else if (tile.hasLift()) {
      for (skill in player.hand) {
        total += skill.skillCard.iconsList.count { it.hasWild() && it.wild }
      }
    }

    return total
  }
}
