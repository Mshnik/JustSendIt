package com.redpup.justsendit.control.player

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the player controllers. */
class PlayerControllerModule : KtAbstractModule() {

  @Provides
  fun playerHandlers(): List<PlayerController> = List(4) { BasicPlayerController() }
}