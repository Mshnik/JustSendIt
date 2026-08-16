package com.redpup.justsendit.control.ai

import com.google.common.collect.Range
import com.redpup.justsendit.control.*
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.MountainTiles.matches
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.random.Dice.averageValue
import com.redpup.justsendit.model.random.Dice.variance
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.skill.SkillEffect
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.matchers.KMatcher
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
    vararg zones: SkillCardZone,
  ): List<Skill> {
    return when (event) {
      is PlaySkillForSkiRideAttempt -> {
        val needed = event.slope.difficulty

        // Target success probability decreases as risk increases.
        val targetProb = 1.0 - (risk * 0.8) // Even at max risk, we want SOME chance.

        val candidates = elements.map {
          val prob = estimateSuccessProbability(it.skillCard, event.slope, needed)
          it to prob
        }

        val acceptable = candidates.filter { it.second >= targetProb }

        val choice = if (acceptable.isNotEmpty()) {
          // Pick the "weakest" acceptable card to save others.
          acceptable.minByOrNull { calculateExpectedValue(it.first.skillCard, event.slope) }?.first
        } else {
          // Pick the strongest card if none are safe enough.
          candidates.maxByOrNull { it.second }?.first
        }

        listOfNotNull(choice)
      }

      is ChooseCardToBuy -> {
        val affordable = elements
          .filter {
            (it.skillCard.cost - (gameModel.shop[it] ?: 0)).coerceAtLeast(0) <= event.studyValue
          }
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

  override suspend fun activateEffects(
    gameModel: GameModel,
    player: Player,
    dice: List<DieRollOrBuilder>,
    effects: List<SkillEffect>,
  ): Boolean {
    TODO("Not yet implemented")
  }

  override suspend fun <D : DieRollOrBuilder> chooseDice(
    gameModel: GameModel,
    player: Player,
    dice: List<D>,
    matcher: KMatcher<D>,
    count: Range<Int>,
  ): List<D> {
    TODO("Not yet implemented")
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

      val moveEvaluations = moves.map { (point, _) ->
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
      card.skillCard.diceList.sumOf { it.averageValue } + card.skillCard.iconsCount
    }
  }

  private fun calculateExpectedValue(
    card: SkillCard, slope: SlopeTile,
  ): Double {
    return card.diceList.sumOf { it.averageValue } + card.iconsList.count {
      it.matches(
        slope
      )
    }
  }

  private fun estimateSuccessProbability(
    card: SkillCard,
    slope: SlopeTile,
    needed: Int,
  ): Double {
    val mean = calculateExpectedValue(card, slope)
    val variance = card.diceList.sumOf { it.variance }
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
}
