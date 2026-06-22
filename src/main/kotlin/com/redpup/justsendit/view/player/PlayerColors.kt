package com.redpup.justsendit.view.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.Player
import javafx.scene.paint.Color

/** Utilities for operating with player colors. */
object PlayerColors {
  private val playerColors = listOf(
    Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE,
    Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.YELLOW
  )

  fun GameModel.getPlayerColor(player: Player): Color = playerColors[players.indexOf(player)]
}