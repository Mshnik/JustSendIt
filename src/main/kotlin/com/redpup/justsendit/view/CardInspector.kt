package com.redpup.justsendit.view

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * Global inspector that shows a high-resolution card view.
 */
object CardInspector {
  private var root: StackPane? = null

  fun init(root: StackPane) {
    this.root = root
  }

  fun inspect(skill: Skill) {
    val root = this.root ?: return

    val overlay = StackPane()
    overlay.style = "-fx-background-color: rgba(0, 0, 0, 0.75);"

    val cardView = VBox()
    cardView.alignment = Pos.CENTER
    cardView.style = "-fx-background-color: white; -fx-border-color: black; -fx-padding: 20;"
    cardView.setMaxSize(400.0, 600.0)

    val title = Label(skill.name)
    title.style = "-fx-font-size: 24; -fx-font-weight: bold;"

    val details = Label("Full rules and high-res art here...\nCost: ${skill.skillCard.cost}")

    cardView.children.addAll(title, details)
    overlay.children.add(cardView)

    overlay.setOnMouseClicked {
      root.children.remove(overlay)
    }

    root.children.add(overlay)
  }
}
