package com.redpup.justsendit.model.player.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Provides
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Testing binding module for the player cards. */
@VisibleForTesting
class FakePlayerModule(private val playerHandlers: List<PlayerHandler>) : KtAbstractModule() {
  override fun configure() {
    bind<PlayerFactory>().to<FakePlayerFactory>()
  }

  @Provides
  @Singleton
  fun playerHandlers(): List<PlayerHandler> {
    return playerHandlers
  }
}