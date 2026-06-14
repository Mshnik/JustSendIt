package com.redpup.justsendit.view

import com.google.inject.Guice
import com.google.inject.Provides
import com.redpup.justsendit.control.ControllerModule
import com.redpup.justsendit.log.LazyForwardingLogger
import com.redpup.justsendit.log.LoggerInstance
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.model.GameModelModule
import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.util.SystemTimeSourceModule
import com.redpup.justsendit.view.board.HexGridViewer
import com.redpup.justsendit.view.info.GameInfoPanel
import com.redpup.justsendit.view.info.InfoPanel
import com.redpup.justsendit.view.log.LogPanel
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

/** A top level JavaFX application for JustSendIt. */
class JustSendItGui : Application() {
  private lateinit var logPanel: LogPanel
  private lateinit var guiState: GuiState
  private lateinit var advanceButton: AdvanceButton

  /** Binding module for [JustSendItGui]. */
  private class JustSendItGuiModule(private val gui: JustSendItGui) : KtAbstractModule() {
    @Provides
    fun provideGui(): JustSendItGui {
      return gui
    }

    @Provides
    fun provideLogPanel(): LogPanel {
      return gui.logPanel
    }

    @Provides
    fun provideAdvanceButton(): AdvanceButton {
      return gui.advanceButton
    }
  }

  override fun init() {
    guiState = Guice.createInjector(
      JustSendItGuiModule(this),
      GameModelModule(),
      GuiCoroutineModule(),
      ControllerModule(),
      SystemTimeSourceModule(),
      LoggerModule(
        LoggerInstance(LazyForwardingLogger { logPanel }),
        LoggerInstance(LazyForwardingLogger { advanceButton })
      )
    ).getInstance(GuiState::class.java)
  }

  override fun start(stage: Stage) {
    val gameModel = guiState.gameModel
    val guiController = guiState.guiController
    val hexGridViewer = HexGridViewer(gameModel)
    guiController.hexGridViewer = hexGridViewer
    val infoPanel = InfoPanel(gameModel)
    val gameInfoPanel = GameInfoPanel(gameModel)
    logPanel = LogPanel(gameModel)

    hexGridViewer.setOnMouseMoved { event ->
      val hex = hexGridViewer.hexFromPixel(event.x, event.y)
      val tile = gameModel.tileMap[hex]
      if (tile != null) {
        infoPanel.updateHexInfo(tile)
      } else {
        infoPanel.clear()
      }

      val playersOnHex = gameModel.players.filter { it.location == hex }
      infoPanel.updatePlayersInfo(playersOnHex)
    }

    advanceButton = AdvanceButton(guiState, gameInfoPanel)
    advanceButton.setupStart()
    val topPanel = VBox(gameInfoPanel, advanceButton)

    val root = BorderPane()
    root.center = hexGridViewer
    root.right = infoPanel
    root.top = topPanel
    root.bottom = logPanel

    stage.scene = Scene(root)
    stage.title = "Just Send It!"
    stage.show()
  }
}

fun main() {
  Application.launch(JustSendItGui::class.java)
}
