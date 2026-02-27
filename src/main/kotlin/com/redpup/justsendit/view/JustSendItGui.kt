package com.redpup.justsendit.view

import com.google.inject.Guice
import com.google.inject.Provides
import com.redpup.justsendit.control.ControllerModule
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.GameModelModule
import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.util.SystemTimeSourceModule
import com.redpup.justsendit.view.board.HexGridViewer
import com.redpup.justsendit.view.info.GameInfoPanel
import com.redpup.justsendit.view.info.InfoPanel
import com.redpup.justsendit.view.log.LogPanel
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javax.inject.Inject

/** A top level JavaFX application for JustSendIt. */
class JustSendItGui : Application() {
  private lateinit var gameModel: GameModel
  private lateinit var logPanel: LogPanel

  class GuiLogger @Inject constructor(private val gui: JustSendItGui) : Logger {
    override fun log(log: Log) {
      gui.logPanel.log(log)
    }
  }

  override fun init() {
    gameModel = Guice.createInjector(
      object : KtAbstractModule() {
        @Provides
        fun provideGui(): JustSendItGui {
          return this@JustSendItGui
        }
      },
      GameModelModule(),
      ControllerModule(),
      SystemTimeSourceModule(),
      LoggerModule(GuiLogger::class)
    ).getInstance(GameModel::class.java)
  }

  override fun start(stage: Stage) {
    val hexGridViewer = HexGridViewer(gameModel)
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

    val nextTurnButton = Button("Next Turn")
    nextTurnButton.setOnAction {
      (gameModel as MutableGameModel).turn()
      gameInfoPanel.update()
    }

    val topPanel = VBox(gameInfoPanel, nextTurnButton)

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
