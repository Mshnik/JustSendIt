package com.redpup.justsendit.view

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.model.supply.SkillDecksInstance
import com.redpup.justsendit.view.board.HexGridViewer
import com.redpup.justsendit.view.info.InfoPanel
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

/** A top level JavaFX application for JustSendIt. */
class JustSendItGui : Application() {
  private lateinit var gameModel: GameModel

  override fun init() {
    SkillDecksInstance.reset()
    gameModel = MutableGameModel(skillDecks = SkillDecksInstance)
  }

  override fun start(stage: Stage) {
    val hexGridViewer = HexGridViewer(gameModel)
    val infoPanel = InfoPanel(gameModel)

    hexGridViewer.setOnMouseMoved { event ->
      val hex = hexGridViewer.hexFromPixel(event.x, event.y)
      val tile = gameModel.tileMap[hex]
      if (tile != null) {
        infoPanel.updateHexInfo(tile)
      } else {
        infoPanel.clear()
      }

      val player = gameModel.players.find { it.location == hex }
      if (player != null) {
        infoPanel.updatePlayerInfo(player)
      }
    }

    val root = BorderPane()
    root.center = hexGridViewer
    root.right = infoPanel

    stage.scene = Scene(root)
    stage.title = "Just Send It!"
    stage.show()
  }
}

fun main() {
  Application.launch(JustSendItGui::class.java)
}
