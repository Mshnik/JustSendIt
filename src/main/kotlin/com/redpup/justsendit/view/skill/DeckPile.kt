package com.redpup.justsendit.view.skill

import com.redpup.justsendit.model.player.Player
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane

/**
 * Widget for the player's deck.
 */
class DeckPile : StackPane() {
  private val imageView = ImageView()
  private val badge = Label()

  init {
    this.styleClass.add("pile-widget")
    
    val image = Image(javaClass.getResource("/com/redpup/justsendit/img/skill_cards/Skill Card Back.png")!!.toExternalForm())
    imageView.image = image
    imageView.isPreserveRatio = true
    imageView.fitWidth = 100.0
    
    badge.styleClass.add("card-badge")
    
    children.addAll(imageView, badge)
    StackPane.setAlignment(badge, Pos.TOP_RIGHT)
  }

  fun update(player: Player) {
    badge.text = player.skillDeck.size.toString()
    badge.isVisible = player.skillDeck.isNotEmpty()
  }
}
