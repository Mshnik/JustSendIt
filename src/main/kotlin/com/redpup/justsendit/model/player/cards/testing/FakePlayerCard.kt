package com.redpup.justsendit.model.player.cards.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

/** A testing implementation of [PlayerCard]. */
@VisibleForTesting
class FakePlayerCard(override val proto: PlayerCardProto) : PlayerCard {
  private var gameEventFn: (PlayerGameEvent) -> Unit = {}

  /** Sets [gameEventFn]. */
  fun setGameEventFn(gameEventFn: (PlayerGameEvent) -> Unit) {
    this.gameEventFn = gameEventFn
  }

  override fun handleGameEvent(event: PlayerGameEvent) {
    gameEventFn(event)
  }
}