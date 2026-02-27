package com.redpup.justsendit.control

import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.view.GuiPlayerControllerModule

/** Top level controller module. */
class ControllerModule : KtAbstractModule() {
  override fun configure() {
    install(GuiPlayerControllerModule())
  }
}