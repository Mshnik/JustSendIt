package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

/**
 *
 */
interface PlayerCard {
  /** The [PlayerCardProto] underneath this player card. */
  val proto: PlayerCardProto

  /** The name of this player card. */
  val name: String get() = proto.name

  /** Resets the state of this [PlayerCard] for the start of their turn. */
  fun startTurn() {}

  /** Handles the given [event]. */
  fun handleGameEvent(event: PlayerGameEvent)
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