package com.redpup.justsendit.control.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Provides
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Testing module for player controllers. */
@VisibleForTesting
class FakePlayerControllerModule(private val playerControllers: List<PlayerController>) :
  KtAbstractModule() {
  @Provides
  @Singleton
  fun playerHandlers(): List<PlayerController> {
    return playerControllers
  }
}