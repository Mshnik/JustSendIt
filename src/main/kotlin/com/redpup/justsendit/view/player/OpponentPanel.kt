package com.redpup.justsendit.view.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.Player
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import javafx.application.Platform

/**
 * Top panel showing other players.
 */
class OpponentPanel(private val gameModel: GameModel) : HBox(), Logger {

  init {
    this.styleClass.add("opponent-panel")
    this.alignment = Pos.CENTER
    this.spacing = 20.0
    this.prefHeight = 80.0
    
    update()
  }

  override fun log(log: Log) {
    Platform.runLater { update() }
  }

  fun update() {
    children.clear()
    val currentPlayer = gameModel.currentPlayer
    gameModel.players.filter { it != currentPlayer }.forEach { opponent ->
        children.add(createOpponentWidget(opponent))
    }
  }

  private fun createOpponentWidget(player: Player): VBox {
    val widget = VBox()
    widget.alignment = Pos.CENTER
    widget.styleClass.add("opponent-widget")
    
    val nameLabel = Label(player.name)
    val scoreLabel = Label("Points: ${player.points}")
    val wobbleLabel = Label("Wobbles: ${player.wobbles}")
    
    widget.children.addAll(nameLabel, scoreLabel, wobbleLabel)
    return widget
  }
}
