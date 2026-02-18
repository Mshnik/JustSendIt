package com.redpup.justsendit.view.player

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

/** Class responsible for rendering the players on the mountain. */
class PlayerRenderer(
  private val gc: GraphicsContext,
  private val hexSize: Double,
  private val margin: Double,
) {
  private val playerColors = listOf(
    Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE,
    Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.YELLOW
  )
  private val playerOnHexVerticalSpacing = 15.0
  private val playerOvalWidthPadding = 10.0
  private val playerOvalHeight = 14.0

  /** Renders the players in [gameModel] on the mountain. */
  fun render(gameModel: GameModel) {
    val bounds = gameModel.tileMap.bounds()
    val playersByLocation = gameModel.players
      .mapIndexedNotNull { i, player -> player.location?.let { Triple(player, it, i) } }
      .groupBy({ it.second }, { Pair(it.first, it.third) })

    playersByLocation.forEach { (location, players) ->
      val x = hexSize * (location.toX() - bounds.minX) + margin
      val y = hexSize * (location.toY() - bounds.minY) + margin
      val totalPlayersOnHex = players.size
      val totalHeight = totalPlayersOnHex * playerOnHexVerticalSpacing

      players.forEachIndexed { i, (player, playerIndex) ->
        val name = player.playerCard.name
        val nameWidth = name.length * 6.5
        val ovalWidth = nameWidth + playerOvalWidthPadding
        val color = playerColors[playerIndex % playerColors.size]

        val playerX = x - ovalWidth / 2
        val playerY = y - totalHeight / 2 + i * playerOnHexVerticalSpacing

        // Draw white filled oval
        gc.fill = Color.WHITE
        gc.fillOval(playerX, playerY, ovalWidth, playerOvalHeight)

        // Draw colored oval border
        gc.stroke = color
        gc.strokeOval(playerX, playerY, ovalWidth, playerOvalHeight)

        // Draw player name
        gc.fill = color
        gc.font = Font.font("Verdana", FontWeight.BOLD, 12.0)
        gc.fillText(name, playerX + playerOvalWidthPadding / 2, playerY + playerOvalHeight - 2)
      }
    }
  }
}
