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

import javafx.scene.image.Image

class HexGridViewer(private val gameModel: GameModel) : Canvas() {

  private val hexSize = 49.15 // Radius from center to corner
  private val margin = 100.0 // Extra space on sides.
  private val xNudge = 10.0
  private val yNudge = 0.0
  private val bounds: Bounds = gameModel.tileMap.bounds()
  private val playerRenderer: PlayerRenderer
  private var highlightedHexes : Collection<HexPoint> = setOf()
  var onHexClicked: ((HexPoint) -> Unit)? = null

  private val boardImage = Image(javaClass.getResource("/com/redpup/justsendit/img/Board.png")!!.toExternalForm())

  init {
    val scale = 0.15
    width = 6377.0 * scale
    height = 4656.0 * scale
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

  fun highlightHexes(hexes: Collection<HexPoint>) {
    this.highlightedHexes = hexes
  }

  private fun drawGrid(gc: GraphicsContext) {
    gc.clearRect(0.0, 0.0, width, height)
    
    // Draw background board image
    gc.drawImage(boardImage, 0.0, 0.0, width, height)

    val hexRenderer = HexRenderer(gc, hexSize)
    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = xNudge + hexSize * (pt.toX() - bounds.minX) + margin
      val y = yNudge + hexSize * (pt.toY() - bounds.minY) + margin

      gameModel.tileMap[pt]?.let { tile ->
        hexRenderer.draw(tile, x, y, pt in highlightedHexes)
      }
    }
    playerRenderer.render(gameModel)
  }

  fun hexFromPixel(x: Double, y: Double): HexPoint {
    val q = ((x - margin - xNudge) / hexSize + bounds.minX) / 1.5
    val r = ((y - margin - yNudge) / hexSize + bounds.minY) / 1.732 - q / 2
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