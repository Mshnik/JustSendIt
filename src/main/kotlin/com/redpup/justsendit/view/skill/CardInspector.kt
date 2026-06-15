package com.redpup.justsendit.view.skill

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * Global inspector that shows a high-resolution card view.
 */
object CardInspector {
  private var root: StackPane? = null

  fun init(root: StackPane) {
    CardInspector.root = root
  }

  fun inspect(skill: Skill) {
    val root = root ?: return

    val overlay = StackPane()
    overlay.styleClass.add("card-inspector-overlay")

    val imagePath = skill.skillCard.filename.removePrefix("src/main/resources")
    val image = Image(javaClass.getResource(imagePath)!!.toExternalForm())
    val imageView = ImageView(image)
    imageView.isPreserveRatio = true
    imageView.fitHeight = 700.0 // High resolution view

    val cardView = VBox(imageView)
    cardView.alignment = Pos.CENTER
    cardView.styleClass.add("card-inspector-view")
    cardView.setMaxSize(500.0, 750.0)

    overlay.children.add(cardView)

    overlay.setOnMouseClicked {
        root.children.remove(overlay)
    }

    root.children.add(overlay)
  }
}

