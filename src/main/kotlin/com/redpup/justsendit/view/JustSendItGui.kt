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
import com.redpup.justsendit.view.skill.CardInspector
import com.redpup.justsendit.view.sidebar.GameInfoPanel
import com.redpup.justsendit.view.sidebar.InfoPanel
import com.redpup.justsendit.view.sidebar.LogPanel
import com.redpup.justsendit.view.player.ActivePlayerArea
import com.redpup.justsendit.view.player.OpponentPanel
import com.redpup.justsendit.view.player.PlayerCardChooser
import com.redpup.justsendit.view.sidebar.SidebarHub
import com.redpup.justsendit.view.skill.DiscardInspector
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

/** A top level JavaFX application for JustSendIt. */
class JustSendItGui : Application() {
  private lateinit var logPanel: LogPanel
  private lateinit var guiState: GuiState
  private lateinit var guiController: GuiController
  private lateinit var advanceButton: AdvanceButton
  private lateinit var opponentPanel: OpponentPanel
  private lateinit var sidebarHub: SidebarHub
  private lateinit var activePlayerArea: ActivePlayerArea

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

    @Provides
    fun provideActivePlayerArea(): ActivePlayerArea {
      return gui.activePlayerArea
    }
  }

  override fun init() {
    var injector = Guice.createInjector(
      JustSendItGuiModule(this),
      GameModelModule(),
      GuiCoroutineModule(),
      ControllerModule(),
      SystemTimeSourceModule(),
      LoggerModule(
        LoggerInstance(LazyForwardingLogger { logPanel }),
        LoggerInstance(LazyForwardingLogger { advanceButton }),
        LoggerInstance(LazyForwardingLogger { opponentPanel }),
        LoggerInstance(LazyForwardingLogger { sidebarHub }),
        LoggerInstance(LazyForwardingLogger { activePlayerArea })
      )
    )
    guiState = injector.getInstance(GuiState::class.java)
    guiController = injector.getInstance(GuiController::class.java)
  }

  override fun start(stage: Stage) {
    val gameModel = guiState.gameModel
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

    opponentPanel = OpponentPanel(gameModel)
    val headerPanel = HeaderPanel(gameInfoPanel, opponentPanel, advanceButton)
    
    sidebarHub = SidebarHub(gameModel, logPanel)
    activePlayerArea = ActivePlayerArea(guiState)
    guiController.activePlayerArea = activePlayerArea

    val mainLayout = BorderPane()
    mainLayout.top = headerPanel
    mainLayout.center = hexGridViewer
    mainLayout.right = sidebarHub
    mainLayout.bottom = activePlayerArea

    val root = StackPane(mainLayout)
    CardInspector.init(root)
    DiscardInspector.init(root)
    PlayerCardChooser.init(root)
    val scene = Scene(root, 1400.0, 1200.0)
    scene.stylesheets.add(
      javaClass.getResource("/com/redpup/justsendit/view/light-theme.css")!!.toExternalForm()
    )
    stage.scene = scene

    stage.title = "Just Send It!"
    stage.show()

    // Initial update
    activePlayerArea.update(gameModel.currentPlayer)
  }
}

fun main() {
  Application.launch(JustSendItGui::class.java)
}
