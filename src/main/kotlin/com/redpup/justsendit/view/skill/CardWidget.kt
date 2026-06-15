package com.redpup.justsendit.view.skill

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

import javafx.scene.image.Image
import javafx.scene.image.ImageView

import javafx.animation.Timeline
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.scene.shape.Rectangle
import javafx.util.Duration

/**
 * A widget representing a single skill card.
 * Can be used in hand or in accordion lists.
 */
class CardWidget(val skill: Skill, val isAccordion: Boolean = false) : VBox() {

  private val badge = Label()
  private val imageView = ImageView()
  private var timeline: Timeline? = null
  
  private val fullWidth = if (isAccordion) 250.0 else 150.0
  private val fullHeight = fullWidth * 1.4 // Assuming standard ratio
  private val collapsedHeight = fullHeight * 0.25

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
    if (isAccordion) {
        this.styleClass.add("accordion-card")
        this.prefHeight = collapsedHeight
        this.minHeight = collapsedHeight
        this.maxWidth = fullWidth
        this.alignment = Pos.TOP_LEFT
    }
    
    val imagePath = skill.skillCard.filename.removePrefix("src/main/resources")
    val image = Image(javaClass.getResource(imagePath)!!.toExternalForm())
    imageView.image = image
    imageView.isPreserveRatio = true
    imageView.fitWidth = fullWidth
    
    badge.styleClass.add("card-badge")
    badge.isVisible = false
    
    val stack = StackPane(imageView, badge)
    stack.alignment = Pos.TOP_LEFT
    StackPane.setAlignment(badge, Pos.TOP_RIGHT)

    children.add(stack)
    
    // Clipping for accordion effect
    if (isAccordion) {
        val clip = Rectangle(fullWidth, collapsedHeight)
        this.clip = clip
        
        // Ensure clip size follows prefHeight
        this.prefHeightProperty().addListener { _, _, newHeight ->
            clip.height = newHeight.toDouble()
        }
    }
    
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
        if (!isAccordion) this.translateY = -12.0
    } else {
        this.styleClass.remove("selected")
        if (!isAccordion) this.translateY = 0.0
    }
  }

  fun expand() {
    if (!isAccordion) return
    this.viewOrder = -1.0 // Bring to front visually without changing layout order
    timeline?.stop()
    timeline = Timeline(
        KeyFrame(Duration.millis(150.0), 
            KeyValue(this.prefHeightProperty(), fullHeight),
            KeyValue(this.minHeightProperty(), fullHeight)
        )
    )
    timeline?.play()
  }

  fun collapse() {
    if (!isAccordion) return
    this.viewOrder = 0.0 // Reset Z-depth
    timeline?.stop()
    timeline = Timeline(
        KeyFrame(Duration.millis(150.0), 
            KeyValue(this.prefHeightProperty(), collapsedHeight),
            KeyValue(this.minHeightProperty(), collapsedHeight)
        )
    )
    timeline?.play()
  }
}

/**
 * A vertical list of cards that expand on hover.
 */
class AccordionCardList : VBox() {
  init {
    spacing = 0.0 // Elements are now spaced by their own clipped heights
    alignment = Pos.TOP_CENTER
  }

  fun setCards(cards: List<Skill>) {
    children.clear()
    cards.forEach { skill ->
      val widget = CardWidget(skill, isAccordion = true)
      widget.setOnMouseEntered {
        widget.expand()
      }
      widget.setOnMouseExited {
        widget.collapse()
      }
      children.add(widget)
    }
  }
}
