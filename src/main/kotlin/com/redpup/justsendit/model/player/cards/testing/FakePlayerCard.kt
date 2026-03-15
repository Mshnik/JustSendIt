package com.redpup.justsendit.model.player.cards.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

/** A testing implementation of [PlayerCard]. */
@VisibleForTesting
class FakePlayerCard(override val proto: PlayerCardProto) : PlayerCard {
  private var gameEventFn: ((PlayerGameEvent, MutablePlayer, GameModel) -> Unit)? = null

  /** Sets [gameEventFn]. */
  fun setGameEventFn(gameEventFn: ((PlayerGameEvent, MutablePlayer, GameModel) -> Unit)?) {
    this.gameEventFn = gameEventFn
  }

  override fun handleGameEvent(
    event: PlayerGameEvent,
    player: MutablePlayer,
    gameModel: GameModel,
  ) {
    gameEventFn?.invoke(event, player, gameModel)
  }
}