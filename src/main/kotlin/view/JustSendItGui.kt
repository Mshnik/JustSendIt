package com.redpup.justsendit.view

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.model.supply.SkillDecksInstance
import com.redpup.justsendit.view.board.HexGridViewer
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
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

    stage.scene = Scene(StackPane(hexGridViewer))
    stage.title = "Just Send It!"
    stage.show()
  }
}

fun main() {
  Application.launch(JustSendItGui::class.java)
}
