package com.redpup.justsendit.view.info

import com.redpup.justsendit.model.GameModel
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class GameInfoPanel(private val gameModel: GameModel) : VBox() {
    private val clockLabel = Label()
    private val playerLabel = Label()

    init {
        children.addAll(clockLabel, playerLabel)
        update()
    }

    fun update() {
        clockLabel.text = "Day: ${gameModel.clock.day}, Turn: ${gameModel.clock.turn}"
        playerLabel.text = "Current Player: ${gameModel.currentPlayer.playerCard.name}"
    }
}
