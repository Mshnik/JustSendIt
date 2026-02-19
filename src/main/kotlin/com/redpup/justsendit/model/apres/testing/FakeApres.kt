package com.redpup.justsendit.model.apres.testing

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer


/** A testing implementation of an Apres card. */
class FakeApres(override val apresCard: ApresCard) : Apres {
  private var applyFn: ((ApresCard, MutablePlayer, Boolean, GameModel) -> Unit)? = null;

  /** Sets [applyFn] to [fn]*/
  fun onApply(fn: (ApresCard, MutablePlayer, Boolean, GameModel) -> Unit): FakeApres {
    this.applyFn = fn
    return this
  }

  override fun apply(player: MutablePlayer, isFirstPlayerToArrive: Boolean, gameModel: GameModel) {
    applyFn?.let { it(apresCard, player, isFirstPlayerToArrive, gameModel) }
  }
}