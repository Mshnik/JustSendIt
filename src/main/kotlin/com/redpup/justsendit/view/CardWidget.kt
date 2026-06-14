package com.redpup.justsendit.view

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

/**
 * A widget representing a single skill card.
 * Can be used in hand or in accordion lists.
 */
class CardWidget(val skill: Skill) : VBox() {

  private val header = StackPane(Label(skill.name))
  private val content =
    VBox(Label("Cost: ${skill.skillCard.cost}"), Label("Rules text goes here..."))
  private val badge = Label()

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
    this.style = "-fx-background-color: white; -fx-border-color: black; -fx-padding: 5;"
    header.style = "-fx-background-color: lightblue;"
    header.setPrefSize(200.0, 30.0)

    content.setPrefSize(200.0, 150.0)
    content.isManaged = false
    content.isVisible = false

    badge.style =
      "-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 2; -fx-background-radius: 10;"
    badge.isVisible = false

    val headerStack = StackPane(header, badge)
    StackPane.setAlignment(badge, Pos.TOP_RIGHT)

    children.addAll(headerStack, content)

    setOnMouseClicked { event ->
      if (event.button == MouseButton.SECONDARY) {
        CardInspector.inspect(skill)
        event.consume()
      }
    }
  }

  private fun updateVisualState() {
    if (isSelected) {
      this.style =
        "-fx-background-color: #ffffcc; -fx-border-color: gold; -fx-border-width: 2; -fx-padding: 5;"
      this.translateY = -12.0
    } else {
      this.style =
        "-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1; -fx-padding: 5;"
      this.translateY = 0.0
    }
  }

  fun expand() {
    content.isManaged = true
    content.isVisible = true
  }

  fun collapse() {
    content.isManaged = false
    content.isVisible = false
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
