package com.redpup.justsendit.view

import com.redpup.justsendit.model.player.cards.PlayerCard
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CompletableDeferred

/**
 * Global chooser for player cards.
 */
object PlayerCardChooser {
  private var root: StackPane? = null

  fun init(root: StackPane) {
    this.root = root
  }

  suspend fun choose(cards: List<PlayerCard>): PlayerCard {
    val root = this.root ?: throw IllegalStateException("PlayerCardChooser not initialized")
    val deferred = CompletableDeferred<PlayerCard>()

    val overlay = StackPane()
    overlay.styleClass.add("player-card-chooser-overlay")

    val container = VBox()
    container.alignment = Pos.CENTER
    container.spacing = 30.0

    val title = Label("CHOOSE A PLAYER CARD")
    title.styleClass.add("player-card-chooser-title")

    val cardBox = HBox()
    cardBox.alignment = Pos.CENTER
    cardBox.spacing = 20.0

    cards.forEach { card ->
        val widget = PlayerCardWidget(card)
        widget.setOnMouseClicked {
            root.children.remove(overlay)
            deferred.complete(card)
        }
        cardBox.children.add(widget)
    }

    container.children.addAll(title, cardBox)
    overlay.children.add(container)

    root.children.add(overlay)

    return deferred.await()
  }
}
