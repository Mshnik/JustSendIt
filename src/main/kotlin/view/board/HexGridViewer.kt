package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.Bounds
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext

/** A gui component that renders the hex grid mountain map. */
class HexGridViewer(private val gameModel: GameModel) : Canvas() {

  private val hexSize = 60.0 // Radius from center to corner
  private val margin = 60.0 // Extra space on sides.
  private val bounds: Bounds = gameModel.tileMap.bounds()

  init {
    width = bounds.width * hexSize + margin
    height = bounds.height * hexSize + margin
    drawGrid(graphicsContext2D)
  }

  private fun drawGrid(gc: GraphicsContext) {
    val hexRenderer = HexRenderer(gc, hexSize)
    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = hexSize * (pt.toX() - bounds.minX) + margin
      val y = hexSize * (pt.toY() - bounds.minY) + margin

      gameModel.tileMap[pt]?.let { hexRenderer.draw(it, x, y) }
    }
  }
}