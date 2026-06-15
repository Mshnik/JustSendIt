package com.redpup.justsendit.view

import com.redpup.justsendit.view.player.OpponentPanel
import com.redpup.justsendit.view.sidebar.GameInfoPanel
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

/**
 * Top header panel that combines game info, opponents, and the advance button.
 */
class HeaderPanel(
  gameInfoPanel: GameInfoPanel,
  opponentPanel: OpponentPanel,
  advanceButton: AdvanceButton
) : BorderPane() {

  init {
    this.styleClass.add("header-panel")
    
    val leftBox = HBox(gameInfoPanel)
    leftBox.alignment = Pos.CENTER_LEFT
    leftBox.setPrefWidth(300.0)
    
    val rightBox = HBox(advanceButton)
    rightBox.alignment = Pos.CENTER_RIGHT
    rightBox.setPrefWidth(300.0)
    
    this.left = leftBox
    this.center = opponentPanel
    this.right = rightBox
  }
}
