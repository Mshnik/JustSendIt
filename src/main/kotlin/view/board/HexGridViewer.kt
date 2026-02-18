package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.Bounds
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.view.player.PlayerRenderer
import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext

class HexGridViewer(private val gameModel: GameModel) : Canvas() {

  private val hexSize = 60.0 // Radius from center to corner
  private val margin = 60.0 // Extra space on sides.
  private val bounds: Bounds = gameModel.tileMap.bounds()
  private val playerRenderer: PlayerRenderer

  init {
    width = bounds.width * hexSize + margin
    height = bounds.height * hexSize + margin
    playerRenderer = PlayerRenderer(graphicsContext2D, hexSize, margin)

    val timer = object : AnimationTimer() {
      override fun handle(now: Long) {
        drawGrid(graphicsContext2D)
      }
    }
    timer.start()
  }

  private fun drawGrid(gc: GraphicsContext) {
    gc.clearRect(0.0, 0.0, width, height)
    val hexRenderer = HexRenderer(gc, hexSize)
    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = hexSize * (pt.toX() - bounds.minX) + margin
      val y = hexSize * (pt.toY() - bounds.minY) + margin

      gameModel.tileMap[pt]?.let { tile ->
        hexRenderer.draw(tile, x, y)
      }
    }
    playerRenderer.render(gameModel)
  }
}