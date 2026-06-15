package com.redpup.justsendit.view.skill

import com.redpup.justsendit.model.player.Player
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane

/**
 * Widget for the player's discard pile.
 */
class DiscardPile : StackPane() {
  private val imageView = ImageView()
  private val badge = Label()

  init {
    this.styleClass.add("pile-widget")
    
    imageView.isPreserveRatio = true
    imageView.fitWidth = 100.0
    
    badge.styleClass.add("card-badge")
    
    children.addAll(imageView, badge)
    StackPane.setAlignment(badge, Pos.TOP_RIGHT)
  }

  fun update(player: Player) {
    val lastCard = player.skillDiscard.lastOrNull()
    if (lastCard != null) {
        val imagePath = lastCard.skillCard.filename.removePrefix("src/main/resources")
        imageView.image = Image(javaClass.getResource(imagePath)!!.toExternalForm())
    } else {
        imageView.image = null
    }
    
    badge.text = player.skillDiscard.size.toString()
    badge.isVisible = player.skillDiscard.isNotEmpty()
    
    setOnMouseClicked { event ->
        if (event.button == MouseButton.SECONDARY && player.skillDiscard.isNotEmpty()) {
            DiscardInspector.inspect(player.skillDiscard)
        }
    }
  }
}
