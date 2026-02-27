package com.redpup.justsendit.model.apres

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.player.MutablePlayer

/** In memory implementation of apres card. */
interface Apres {
  val apresCard: ApresCard

  /** Applies this Apres benefit to [player]. */
  suspend fun apply(
    player: MutablePlayer,
    isFirstPlayerToArrive: Boolean,
    gameModel: GameModel,
  )
}
