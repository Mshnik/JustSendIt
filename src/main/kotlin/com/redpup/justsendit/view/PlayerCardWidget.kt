package com.redpup.justsendit.view

import com.redpup.justsendit.model.player.cards.PlayerCard
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * A widget representing a single player card.
 */
class PlayerCardWidget(val playerCard: PlayerCard) : VBox() {

  init {
    this.styleClass.add("card-widget")
    this.alignment = Pos.CENTER
    
    val image = Image(javaClass.getResource("/com/redpup/justsendit/img/skill_cards/Skill Card Front Complex.png")!!.toExternalForm())
    val imageView = ImageView(image)
    imageView.isPreserveRatio = true
    imageView.fitWidth = 200.0

    children.add(imageView)
    
    // We can still add the name as a label on top if needed, 
    // but the instruction implies using real images.
    // Let's add the name in a badge or similar if it's important.
  }
}
