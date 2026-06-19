package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Icons.matches
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.proto.SkillCard

/** A simple AI player controller for simulation. */
class SimpleAiController(override val name: String) : PlayerController {

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: PlayerController.SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return when (event) {
      PlayerController.SkillEvent.PLAY_SKILL_FOR_SKI_RIDE_ATTEMPT -> {
        // Pick the card with the highest expected skill value if we need more skill.
        val location = player.location!!
        val tile = gameModel.tileMap[location]!!
        val slope = tile.slope
        val currentSkill = player.inPlay.sumOf { calculateExpectedValue(it.skillCard, slope).toInt() }
        if (currentSkill < slope.difficulty) {
          val bestCard = elements.maxByOrNull { calculateExpectedValue(it.skillCard, slope) }
          listOfNotNull(bestCard)
        } else {
          emptyList() // Stop playing cards.
        }
      }

      PlayerController.SkillEvent.CHOOSE_CARD_TO_BUY -> {
        val studyValue = calculateStudyValue(player, gameModel)
        val affordable = elements
          .filter { (it.skillCard.cost - (gameModel.shop[it] ?: 0)).coerceAtLeast(0) <= studyValue }
          .sortedByDescending { it.skillCard.cost }
        
        affordable.take(count.upperEndpoint())
      }

      PlayerController.SkillEvent.PLAY_SKILL_FOR_LIFT,
      PlayerController.SkillEvent.TRASH_SKILL,
      PlayerController.SkillEvent.DISCARD_FOR_CRASH -> {
        // Just pick the first N elements.
        elements.take(count.upperEndpoint())
      }
    }
  }

  override suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    return elements.take(count.upperEndpoint())
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: PlayerController.MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    return when (event) {
      PlayerController.MountainTileEvent.CHOOSE_START_OF_DAY_LOCATION -> elements.first()
      PlayerController.MountainTileEvent.CHOOSE_SKI_RIDE_DESTINATION -> elements.first()
    }
  }

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    return elements.first()
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision {
    val location = player.location ?: return MountainDecision.DECISION_PASS
    val tile = gameModel.tileMap[location] ?: return MountainDecision.DECISION_PASS

    // 1. If on an exit tile, EXIT.
    if (tile.apresLink > 0) {
      return MountainDecision.DECISION_EXIT
    }

    // 2. If on a lift tile and has cards to discard, LIFT.
    if (tile.hasLift() && player.hand.size >= tile.lift.minCards) {
      return MountainDecision.DECISION_LIFT
    }

    // 3. If there are available moves down, SKI_RIDE.
    val moves = gameModel.getAvailableMoves(player)
    if (moves.isNotEmpty()) {
      return MountainDecision.DECISION_SKI_RIDE
    }

    // 4. Otherwise, PASS.
    return MountainDecision.DECISION_PASS
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
