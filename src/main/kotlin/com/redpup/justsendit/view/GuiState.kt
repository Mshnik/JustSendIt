package com.redpup.justsendit.view

import com.google.inject.Provider
import com.redpup.justsendit.model.MutableGameModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

/** Injectable state for the Gui. */
class GuiState @Inject constructor(
  private val gameModelProvider: Provider<MutableGameModel>,
  val coroutineScope: CoroutineScope,
  val guiController: GuiController,
) {
  val gameModel: MutableGameModel get() = gameModelProvider.get()
}
