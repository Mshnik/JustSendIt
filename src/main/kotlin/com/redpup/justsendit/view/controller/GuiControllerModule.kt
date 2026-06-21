package com.redpup.justsendit.view.controller

import com.google.inject.Provides
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.control.ai.SimpleAiController
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Module providing the [PlayerController]s for the GUI. */
class GuiControllerModule : KtAbstractModule() {
  @Provides
  @Singleton
  fun playerControllers(guiPlayerController: GuiHumanController): List<@JvmSuppressWildcards PlayerController> =
    listOf(guiPlayerController, GuiAIController(SimpleAiController("SimpleAI-1")))
}
