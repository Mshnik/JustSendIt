package com.redpup.justsendit.model.apres

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

import com.redpup.justsendit.model.supply.proto.SkillCard

/** In-memory implementation of an apres card. */
interface Apres {
  val apresCard: ApresCard

  /** The current value of the stockpile for this card. */
  val stockpile: Int

  /** Applies this Apres benefit to [player]. */
  suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  )

  /** Handles a game event, potentially updating the stockpile. */
  fun handleGameEvent(event: ApresGameEvent, gameModel: GameModel)
}

/** Base implementation of an Apres card with a stockpile. */
abstract class BaseApres(override val apresCard: ApresCard) : Apres {
  override var stockpile: Int = 0

  override fun handleGameEvent(event: ApresGameEvent, gameModel: GameModel) {
    // Default implementation does nothing.
  }
}

/** The shared implementation of an apres that gives the player the stockpile. */
abstract class StockpilingBaseApres(override val apresCard: ApresCard) : BaseApres(apresCard) {
  override suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  ) {
    if (isFirstPlayerToArrive) {
      player.points += stockpile
      stockpile = 0
    } else {
      player.points += NON_STOCKPILE_POINTS
    }
  }

  /** Handles the given game event. */
  abstract override fun handleGameEvent(event: ApresGameEvent, gameModel: GameModel)

  companion object {
    const val NON_STOCKPILE_POINTS = 10
  }
}

/** Represents events that can trigger stockpile increases. */
sealed class ApresGameEvent {
  data object PlayerUsedLift : ApresGameEvent()
  data class PlayerPlayedCard(val card: SkillCard) : ApresGameEvent()
  data class PlayerSkiRide(val turn: Int, val success: Boolean) : ApresGameEvent()
}
