package com.redpup.justsendit.model.player

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the player cards. */
class PlayerModule : KtAbstractModule() {
  override fun configure() {
    bind<PlayerFactory>().to<PlayerFactoryImpl>()
  }

  @Provides
  fun playerHandlers(): List<PlayerHandler> = List(4) { BasicPlayerHandler() }
}