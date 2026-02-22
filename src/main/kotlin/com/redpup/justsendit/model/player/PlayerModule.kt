package com.redpup.justsendit.model.player

import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the player cards. */
class PlayerModule : KtAbstractModule() {
  override fun configure() {
    bind<PlayerFactory>().to<PlayerFactoryImpl>()
  }
}
