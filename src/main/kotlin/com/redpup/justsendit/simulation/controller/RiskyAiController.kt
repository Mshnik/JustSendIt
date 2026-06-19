package com.redpup.justsendit.simulation.controller

import com.google.common.collect.Range
import com.redpup.justsendit.control.player.*
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Icons.matches
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.supply.proto.SkillCard
import kotlin.math.sqrt

/**
 * An AI controller that makes decisions based on a [risk] parameter in [0, 1].
 * Higher risk values lead to more aggressive plays and choosing more difficult terrain.
 */
class RiskyAiController(override val name: String, private val risk: Double) : PlayerController {

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return when (event) {
      PlaySkillForSkiRideAttempt -> {
        val location = player.location!!
        val tile = gameModel.tileMap[location]!!
        val slope = tile.slope
        val needed = (slope.difficulty - player.inPlay.sumOf {
          calculateExpectedValue(
            it.skillCard,
            slope
          ).toInt()
        }).coerceAtLeast(1)

        // Target success probability decreases as risk increases.
        val targetProb = 1.0 - (risk * 0.8) // Even at max risk, we want SOME chance.

        val candidates = elements.map {
          val prob = estimateSuccessProbability(it.skillCard, slope, needed)
          it to prob
        }

        val acceptable = candidates.filter { it.second >= targetProb }

        val choice = if (acceptable.isNotEmpty()) {
          // Pick the "weakest" acceptable card to save others.
          acceptable.minByOrNull { calculateExpectedValue(it.first.skillCard, slope) }?.first
        } else {
          // Pick the strongest card if none are safe enough.
          candidates.maxByOrNull { it.second }?.first
        }

        listOfNotNull(choice)
      }

      ChooseCardToBuy -> {
        val studyValue = calculateStudyValue(player, gameModel)
        val affordable = elements
          .filter { (it.skillCard.cost - (gameModel.shop[it] ?: 0)).coerceAtLeast(0) <= studyValue }
          .sortedByDescending { it.skillCard.cost }

        affordable.take(count.upperEndpoint())
      }

      else -> elements.take(count.upperEndpoint())
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
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    val handStrength = calculateHandStrength(player)

    val evaluations = elements.map { point ->
      val destTile = gameModel.tileMap[point]!!
      val difficulty = if (destTile.hasSlope()) destTile.slope.difficulty else 0
      // Score: higher difficulty is "better" for risky players, but only if they can handle it.
      val score =
        (difficulty * risk) - (difficulty.toDouble() / handStrength.coerceAtLeast(1.0) * (1.0 - risk) * 5.0)
      point to score
    }

    return evaluations.maxByOrNull { it.second }?.first ?: elements.first()
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

    if (tile.apresLink > 0) {
      return MountainDecision.DECISION_EXIT
    }

    if (tile.hasLift() && player.hand.size >= tile.lift.minCards) {
      return MountainDecision.DECISION_LIFT
    }

    val moves = gameModel.getAvailableMoves(player)
    if (moves.isNotEmpty()) {
      // Choose whether to ski/ride or pass based on risk vs hand strength.
      val handStrength = calculateHandStrength(player)

      val moveEvaluations = moves.map { (point, direction) ->
        val destTile = gameModel.tileMap[point]!!
        val difficulty = if (destTile.hasSlope()) destTile.slope.difficulty else 0
        // Score: higher difficulty is "better" for risky players, but only if they can handle it.
        val score =
          (difficulty * risk) - (difficulty.toDouble() / handStrength.coerceAtLeast(1.0) * (1.0 - risk) * 5.0)
        score
      }

      val maxScore = moveEvaluations.maxOrNull() ?: -100.0
      if (maxScore > 0 || risk > 0.5) {
        return MountainDecision.DECISION_SKI_RIDE
      }
    }

    return MountainDecision.DECISION_PASS
  }

  private fun calculateHandStrength(player: Player): Double {
    return player.hand.sumOf { card ->
      val sc = card.skillCard
      (sc.greenDice * 2.5) + (sc.blueDice * 3.5) + (sc.blackDice * 4.5) + sc.iconsCount
    }
  }

  private fun calculateExpectedValue(
    card: SkillCard,
    slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
  ): Double {
    return (card.greenDice * 2.5) + (card.blueDice * 3.5) + (card.blackDice * 4.5) + card.iconsList.count {
      it.matches(
        slope
      )
    }
  }

  private fun estimateSuccessProbability(
    card: SkillCard,
    slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
    needed: Int,
  ): Double {
    val mean = calculateExpectedValue(card, slope)
    val variance = (card.greenDice * 1.25) + (card.blueDice * 2.92) + (card.blackDice * 5.25)
    val stdDev = sqrt(variance).coerceAtLeast(0.001)

    val z = (needed - 0.5 - mean) / stdDev // Continuity correction

    // Very rough normal approximation for P(X >= needed)
    return when {
      z < -2.0 -> 0.98
      z < -1.0 -> 0.84
      z < 0.0 -> 0.5 + (-z * 0.34)
      z < 1.0 -> 0.5 - (z * 0.34)
      z < 2.0 -> 0.16 - ((z - 1.0) * 0.14)
      else -> 0.02
    }
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
