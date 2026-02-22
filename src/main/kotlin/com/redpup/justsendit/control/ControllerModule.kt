package com.redpup.justsendit.control

import com.redpup.justsendit.control.player.PlayerControllerModule
import com.redpup.justsendit.util.KtAbstractModule

/** Top level controller module. */
class ControllerModule : KtAbstractModule() {
  override fun configure() {
    install(PlayerControllerModule())
  }
}