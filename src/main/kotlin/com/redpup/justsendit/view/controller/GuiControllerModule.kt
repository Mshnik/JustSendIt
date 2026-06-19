package com.redpup.justsendit.view.controller

import com.google.inject.Provides
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Module providing the [GuiPlayerController]. */
class GuiControllerModule : KtAbstractModule() {
  @Provides
  @Singleton
  fun playerControllers(guiPlayerController: GuiPlayerController): List<@JvmSuppressWildcards PlayerController> =
    listOf(guiPlayerController)
}
