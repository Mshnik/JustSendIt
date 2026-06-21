package com.redpup.justsendit.model.clock

import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.proto.GameState
import javax.inject.Qualifier

/** Recording of time in game. */
interface Clock {
  /** The current state of the game. */
  val state: GameState

  /** What round of day it is. */
  val round: Int

  /** What turn of the round it is. */
  val turn: Int

  /** What sub-turn of the turn it is. */
  val subTurn: Int

  /** Returns whether it is the first subTurn of the turn. */
  val isFirstSubTurn: Boolean get() = subTurn == 1

  /** What game of day it is. */
  val day: Day

  /** Listeners for state changes. When invoked, called with (old, new) state. */
  val onStateChanged: MutableList<(GameState, GameState) -> Unit>

  /** Updates this Clock's position to start of game. */
  fun startGame()

  /** Starts the next day of the game. */
  fun startDay()

  /** Ends the current day. Returns true iff there is another day to play. */
  fun endDay(): Boolean

  /** Starts the next round of the game. */
  fun startRound()

  /** Ends the current round. */
  fun endRound()

  /** Starts the next turn of the game. */
  fun startTurn()

  /** Ends the current turn. */
  fun endTurn(turnsRemain: Boolean)

  /** Increases the sub turn of the clock. */
  fun incrementSubTurn()
}

/** [Qualifier] for the maximum rounds in a day. */
@Qualifier
annotation class MaxRound
