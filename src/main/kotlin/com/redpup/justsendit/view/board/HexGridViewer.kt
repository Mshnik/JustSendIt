package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.Bounds
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.view.player.PlayerRenderer
import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import kotlin.math.round

class HexGridViewer(private val gameModel: GameModel) : Canvas() {

  private val hexSize = 60.0 // Radius from center to corner
  private val margin = 60.0 // Extra space on sides.
  val bounds: Bounds = gameModel.tileMap.bounds()
  private val playerRenderer: PlayerRenderer
  private var highlightedHexes = setOf<HexPoint>()
  var onHexClicked: ((HexPoint) -> Unit)? = null

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

    setOnMouseClicked { event ->
        val hex = hexFromPixel(event.x, event.y)
        onHexClicked?.invoke(hex)
    }
  }

  fun highlightHexes(hexes: Set<HexPoint>) {
    this.highlightedHexes = hexes
  }

  private fun drawGrid(gc: GraphicsContext) {
    gc.clearRect(0.0, 0.0, width, height)
    val hexRenderer = HexRenderer(gc, hexSize)
    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = hexSize * (pt.toX() - bounds.minX) + margin
      val y = hexSize * (pt.toY() - bounds.minY) + margin

      gameModel.tileMap[pt]?.let { tile ->
        hexRenderer.draw(tile, x, y, pt in highlightedHexes)
      }
    }
    playerRenderer.render(gameModel)
  }

  fun hexFromPixel(x: Double, y: Double): HexPoint {
    val q = ((x - margin) / hexSize + bounds.minX) / 1.5
    val r = ((y - margin) / hexSize + bounds.minY) / 1.732 - q / 2
    return hexRound(q, r)
  }

  private fun hexRound(q: Double, r: Double): HexPoint {
    val s = -q - r
    var rq = round(q)
    var rr = round(r)
    val rs = round(s)

    val qDiff = (rq - q).let { it * it }
    val rDiff = (rr - r).let { it * it }
    val sDiff = (rs - s).let { it * it }

    if (qDiff > rDiff && qDiff > sDiff) {
      rq = -rr - rs
    } else if (rDiff > sDiff) {
      rr = -rq - rs
    }

    return hexPoint {
      this.q = rq.toInt()
      this.r = rr.toInt()
    }
  }
}