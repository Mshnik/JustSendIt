package com.redpup.justsendit.view.player

import com.redpup.justsendit.model.player.Player
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox

/**
 * Widget showing a single opponent's state.
 */
class OpponentWidget(private val player: Player) : VBox() {

  init {
    this.styleClass.add("opponent-widget")
    this.alignment = Pos.TOP_LEFT
    this.spacing = 10.0

    update()
  }

  fun update() {
    children.clear()
    val nameLabel = Label(player.name)
    nameLabel.style = "-fx-font-weight: bold; -fx-font-size: 16;"

    val scoreLabel = Label("Points: ${player.points}")
    val wobbleLabel = Label("Wobbles: ${player.wobbles}")

    val deckLabel = Label("Deck size: ${player.skillDeck.size}")
    val discardLabel = Label("Discard size: ${player.skillDiscard.size}")

    children.addAll(nameLabel, scoreLabel, wobbleLabel, deckLabel, discardLabel)
  }
}
