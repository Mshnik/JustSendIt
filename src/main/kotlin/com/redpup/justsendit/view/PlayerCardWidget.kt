package com.redpup.justsendit.view

import com.redpup.justsendit.model.player.cards.PlayerCard
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

/**
 * A widget representing a single player card.
 */
class PlayerCardWidget(val playerCard: PlayerCard) : VBox() {

  init {
    this.styleClass.add("card-widget")
    this.alignment = Pos.CENTER
    this.setPrefSize(200.0, 300.0)

    val header = StackPane(Label(playerCard.name))
    header.styleClass.add("card-header")
    header.setPrefSize(200.0, 40.0)

    val content = VBox(Label("Player Card details here..."))
    content.alignment = Pos.CENTER
    VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS)

    children.addAll(header, content)
  }
}
