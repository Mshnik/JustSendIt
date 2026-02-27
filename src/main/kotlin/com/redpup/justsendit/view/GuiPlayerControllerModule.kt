package com.redpup.justsendit.view

import com.google.inject.Provides
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Module providing the [GuiController]. */
class GuiPlayerControllerModule : KtAbstractModule() {
  @Provides
  @Singleton
  fun playerControllers(guiController: GuiController): List<@JvmSuppressWildcards PlayerController> =
    listOf(guiController)
}
