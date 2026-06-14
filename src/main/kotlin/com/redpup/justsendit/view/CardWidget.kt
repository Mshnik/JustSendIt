package com.redpup.justsendit.view

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * A widget representing a single skill card.
 * Can be used in hand or in accordion lists.
 */
class CardWidget(val skill: Skill) : VBox() {

  private val badge = Label()
  private val imageView = ImageView()

  var isSelected = false
    set(value) {
      field = value
      updateVisualState()
    }

  var selectionIndex: Int? = null
    set(value) {
      field = value
      badge.text = value?.toString() ?: ""
      badge.isVisible = value != null
    }

  init {
    this.styleClass.add("card-widget")
    
    val imagePath = skill.skillCard.filename.removePrefix("src/main/resources")
    val image = Image(javaClass.getResource(imagePath)!!.toExternalForm())
    imageView.image = image
    imageView.isPreserveRatio = true
    imageView.fitWidth = 150.0 // Adjusted for accordion/hand
    
    badge.styleClass.add("card-badge")
    badge.isVisible = false
    
    val stack = StackPane(imageView, badge)
    StackPane.setAlignment(badge, Pos.TOP_RIGHT)

    children.add(stack)
    
    setOnMouseClicked { event ->
        if (event.button == MouseButton.SECONDARY) {
            CardInspector.inspect(skill)
            event.consume()
        }
    }
  }

  private fun updateVisualState() {
    if (isSelected) {
        this.styleClass.add("selected")
        this.translateY = -12.0
    } else {
        this.styleClass.remove("selected")
        this.translateY = 0.0
    }
  }


  fun expand() {
    imageView.fitWidth = 200.0
  }

  fun collapse() {
    imageView.fitWidth = 150.0
  }
}

/**
 * A vertical list of cards that expand on hover.
 */
class AccordionCardList : VBox() {
  init {
    spacing = -20.0 // Overlap effect
  }

  fun setCards(cards: List<Skill>) {
    children.clear()
    cards.forEach { skill ->
      val widget = CardWidget(skill)
      widget.setOnMouseEntered {
        widget.expand()
        widget.toFront()
      }
      widget.setOnMouseExited {
        widget.collapse()
      }
      children.add(widget)
    }
  }
}
