package com.redpup.justsendit.control.ai

import com.google.common.collect.Range
import com.redpup.justsendit.control.*
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.MountainTiles.matches
import com.redpup.justsendit.model.board.tile.proto.LiftDirection
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.DieRollOrBuilder
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.proto.SkillCardZone
import com.redpup.justsendit.model.random.Dice.averageValue
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.model.skill.SkillEffect
import com.redpup.justsendit.model.supply.proto.SkillCard
import com.redpup.matchers.KMatcher

/** A simple AI player controller for simulation. */
class SimpleAiController(override val name: String) : PlayerController {

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
        // Pick the card with the highest expected skill value.
        val bestCard = elements.maxByOrNull { calculateExpectedValue(it.skillCard, event.slope) }
        listOfNotNull(bestCard)
      }

      is ChooseCardToBuy -> {
        val affordable = elements
          .filter {
            (it.skillCard.cost - (gameModel.shop[it] ?: 0)).coerceAtLeast(0) <= event.studyValue
          }
          .sortedByDescending { it.skillCard.cost }

        affordable.take(count.upperEndpoint())
      }

      PlaySkillForLift, TrashSkill, DiscardForCrash -> {
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
    event: MountainTileEvent,
    elements: Collection<HexPoint>,
  ): HexPoint {
    return when (event) {
      ChooseStartOfDayLocation -> elements.first()
      ChooseSkiRideDestination -> elements.first()
    }
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

    // 1. If on an exit tile, EXIT.
    if (tile.apresLink > 0) {
      return MountainDecision.DECISION_EXIT
    }

    // 2. If on a bottom lift tile and has cards to discard, LIFT.
    if (tile.hasLift()
      && tile.lift.direction == LiftDirection.LIFT_DIRECTION_BOTTOM
      && player.hand.size >= tile.lift.minCards
    ) {
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

  private fun calculateExpectedValue(
    card: SkillCard,
    slope: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
  ): Double {
    val diceValue = card.diceList.sumOf { it.averageValue }
    val iconValue = card.iconsList.count { it.matches(slope) }.toDouble()
    return diceValue + iconValue
  }
}
