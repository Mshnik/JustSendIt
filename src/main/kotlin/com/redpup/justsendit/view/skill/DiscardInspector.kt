package com.redpup.justsendit.view.skill

import com.redpup.justsendit.model.skill.Skill
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox

/**
 * Global inspector that shows all cards in a collection (like discard).
 */
object DiscardInspector {
  private var root: StackPane? = null

  fun init(root: StackPane) {
    this.root = root
  }

  fun inspect(cards: Collection<Skill>, title: String = "DISCARD PILE") {
    val root = this.root ?: return
    
    val overlay = StackPane()
    overlay.styleClass.add("card-inspector-overlay")
    
    val container = VBox()
    container.alignment = Pos.CENTER
    container.spacing = 20.0
    container.styleClass.add("discard-inspector-container")
    
    val titleLabel = Label(title)
    titleLabel.styleClass.add("player-card-chooser-title")
    
    val scrollPane = ScrollPane()
    scrollPane.isFitToHeight = true
    scrollPane.prefHeight = 600.0
    scrollPane.maxWidth = 1200.0
    
    val cardBox = HBox()
    cardBox.spacing = 15.0
    cardBox.padding = javafx.geometry.Insets(20.0)
    
    cards.forEach { skill ->
        val imagePath = skill.skillCard.filename.removePrefix("src/main/resources")
        val image = Image(javaClass.getResource(imagePath)!!.toExternalForm())
        val imageView = ImageView(image)
        imageView.isPreserveRatio = true
        imageView.fitHeight = 500.0
        cardBox.children.add(imageView)
    }
    
    scrollPane.content = cardBox
    
    val closeLabel = Label("Click anywhere to close")
    closeLabel.style = "-fx-text-fill: white;"
    
    container.children.addAll(titleLabel, scrollPane, closeLabel)
    overlay.children.add(container)
    
    overlay.setOnMouseClicked {
        root.children.remove(overlay)
    }
    
    root.children.add(overlay)
  }
}
