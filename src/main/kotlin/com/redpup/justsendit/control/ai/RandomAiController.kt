package com.redpup.justsendit.control.ai

import com.google.common.collect.Range
import com.redpup.justsendit.control.*
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Icons.matches
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill

/** An AI controller that makes random choices. */
class RandomAiController(override val name: String) : PlayerController {

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return when (event) {
      is PlaySkillForSkiRideAttempt -> {
        // Randomly decide whether to stop or play a card.
        if (elements.isNotEmpty() && (0..1).random() == 1) {
          listOf(elements.random())
        } else {
          emptyList()
        }
      }

      is ChooseCardToBuy -> {
        // Randomly pick an affordable card.
        val studyValue = calculateStudyValue(player, gameModel)
        val affordable = elements
          .filter { (it.skillCard.cost - (gameModel.shop[it] ?: 0)).coerceAtLeast(0) <= studyValue }

        if (affordable.isNotEmpty() && (0..1).random() == 1) {
          listOf(affordable.random())
        } else {
          emptyList()
        }
      }

      else -> elements.shuffled()
        .take(count.lowerEndpoint() + (0..count.upperEndpoint() - count.lowerEndpoint()).random())
    }
  }

  override suspend fun chooseApresCard(
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    return elements.shuffled()
      .take(count.lowerEndpoint() + (0..count.upperEndpoint() - count.lowerEndpoint()).random())
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    return elements.shuffled().first()
  }

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    return elements.random()
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision {
    val moves = gameModel.getAvailableMoves(player)
    val location = player.location
    val tile = location?.let { gameModel.tileMap[it] }

    val options = mutableListOf<MountainDecision>()

    // Always can pass
    options.add(MountainDecision.DECISION_PASS)

    if (moves.isNotEmpty()) {
      options.add(MountainDecision.DECISION_SKI_RIDE)
    }

    if (tile?.hasLift() == true && player.hand.size >= tile.lift.minCards) {
      options.add(MountainDecision.DECISION_LIFT)
    }

    if (tile?.apresLink ?: 0 > 0) {
      options.add(MountainDecision.DECISION_EXIT)
    }

    return options.random()
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
