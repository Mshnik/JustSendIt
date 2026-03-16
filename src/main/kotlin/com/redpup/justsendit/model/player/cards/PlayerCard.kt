package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

/**
 *
 */
interface PlayerCard {
  /** The [PlayerCardProto] underneath this player card. */
  val proto: PlayerCardProto

  /** The name of this player card. */
  val name: String get() = proto.name


  /** Refreshes this [PlayerCard] for the start of their day. */
  fun startDay() {}

  /** Resets the state of this [PlayerCard] for the start of their turn. */
  fun startTurn() {}

  /** Handles the given [event]. */
  fun handleGameEvent(event: PlayerGameEvent, player: MutablePlayer, gameModel: GameModel) {}

  /** Activates this card. */
  suspend fun activate(player: MutablePlayer, gameModel: GameModel) {}
}

/** A [PlayerCard] that can be activated once per day. */
abstract class ActivatedPlayerCard : PlayerCard {
  private var isUsed = false

  override suspend fun activate(player: MutablePlayer, gameModel: GameModel) {
    check(!isUsed)
    onActivate(player, gameModel)
  }

  override fun startDay() {
    isUsed = false
  }

  /** Handles flipping this [ActivatedPlayerCard]. */
  abstract suspend fun onActivate(player: MutablePlayer, gameModel: GameModel)
}

/** Represents events that can trigger stockpile increases. */
sealed class PlayerGameEvent {
  data class PlayerSkiRide(
    val turn: Int,
    val slope: SlopeTile,
    val pointsOnSlope: Int,
    val skill: Int,
    val difficulty: Int,
  ) : PlayerGameEvent()
}