package com.redpup.justsendit.model.skill

import com.redpup.justsendit.model.MutableGameModel.Companion.CRASH_WOBBLES
import com.redpup.justsendit.model.board.tile.MountainTiles.applyHazards
import com.redpup.justsendit.model.board.tile.MountainTiles.has
import com.redpup.justsendit.model.board.tile.MountainTiles.matches
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.DieRoll
import com.redpup.justsendit.model.player.proto.SkiRideAttempt
import com.redpup.justsendit.model.player.proto.SkiRideAttemptKt.computed
import com.redpup.justsendit.model.player.proto.skiRideAttempt
import com.redpup.justsendit.model.random.Dice.maxValue
import com.redpup.justsendit.util.countDuplicates

/**
 * A wrapper on the resolution of [player] playing [skills] on [slope], after rolling dice.
 */
class SkiRideResolution(
  private val player: Player,
  private val skills: List<Skill>,
  private val slope: SlopeTile,
  private val rolls: List<DieRoll.Builder>,
) {
  /** The number of wobbles represented by [rolls] on [slope]. */
  private val wobbles: Int
    get() = listOf(
      // Base wobbles
      rolls.count { it.rollList.last() == it.die.maxValue },
      // Ice wobbles.
      if (slope has Condition.CONDITION_ICE) rolls.count { it.rollList.last() == 1 } else 0,
      // Mogul wobbles.
      if (slope has Hazard.HAZARD_MOGULS) rolls.map { it.rollList.last() }.countDuplicates() else 0
    ).sum()

  /** Resolves this into a [SkiRideAttempt]. */
  fun resolve(): SkiRideAttempt {
    val wobbles = this.wobbles
    val iconValue = skills.first().skillCard.iconsList.count { it.matches(slope) }
    val skillSum = rolls.sumOf { it.rollList.last().applyHazards(slope.hazardsList) } + iconValue

    val success = skillSum >= slope.difficulty && (player.wobbles + wobbles) < CRASH_WOBBLES

    return skiRideAttempt {
      this.slopeTile = slope
      this.cards += skills.map { it.skillCard }
      this.rolls += rolls
      computed = computed {
        this.success = success
        this.iconValue = iconValue
        this.wobbles = wobbles
      }
    }
  }
}