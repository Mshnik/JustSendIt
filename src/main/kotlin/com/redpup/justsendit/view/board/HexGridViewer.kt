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
import javafx.scene.image.Image
import javafx.scene.paint.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class HexGridViewer(private val gameModel: GameModel) : Canvas() {

  companion object {
    private const val SCALE = 0.15

    private const val BOARD_WIDTH = 6377.0 * SCALE
    private const val BOARD_HEIGHT = 4656.0 * SCALE
    private const val HEX_SIZE = 327 * SCALE // Radius from center to corner
    private const val MARGIN = 667 * SCALE // Extra space on sides.
    private const val X_NUDGE = 67 * SCALE
    private const val Y_NUDGE = 0 * SCALE
  }

  private val bounds: Bounds = gameModel.tileMap.bounds()
  private val playerRenderer: PlayerRenderer
  private var highlightedHexes: Collection<HexPoint> = setOf()
  var onHexClicked: ((HexPoint) -> Unit)? = null

  private val boardImage =
    Image(javaClass.getResource("/com/redpup/justsendit/img/Board.png")!!.toExternalForm())
  private val tileImageCache = mutableMapOf<String, Image>()

  init {
    width = BOARD_WIDTH
    height = BOARD_HEIGHT
    playerRenderer = PlayerRenderer(graphicsContext2D, HEX_SIZE, MARGIN, X_NUDGE, Y_NUDGE)

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

    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = X_NUDGE + HEX_SIZE * (pt.toX() - bounds.minX) + MARGIN
      val y = Y_NUDGE + HEX_SIZE * (pt.toY() - bounds.minY) + MARGIN

      gameModel.tileMap[pt]?.let { tile ->
        val image = if (tile.filename.isNotEmpty()) {
          tileImageCache.getOrPut(tile.filename) {
            Image(
              javaClass.getResource("/com/redpup/justsendit/img/tiles/${tile.filename}")!!
                .toExternalForm()
            )
          }
        } else null

        gc.draw(image, x, y, pt in highlightedHexes)
      }
    }
    playerRenderer.render(gameModel)
  }

  fun hexFromPixel(x: Double, y: Double): HexPoint {
    val q = ((x - MARGIN - X_NUDGE) / HEX_SIZE + bounds.minX) / 1.5
    val r = ((y - MARGIN - Y_NUDGE) / HEX_SIZE + bounds.minY) / 1.732 - q / 2
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

  /** Draws the given [image] at [cx], [cy]. */
  private fun GraphicsContext.draw(image: Image?, cx: Double, cy: Double, isHighlighted: Boolean) {
    if (image != null) {
      // Draw the tile image centered at cx, cy
      // For flat-top hexes, width is 2*R, height is sqrt(3)*R
      val imgWidth = HEX_SIZE * 2.0
      val imgHeight = HEX_SIZE * 1.73205
      drawImage(image, cx - imgWidth / 2.0, cy - imgHeight / 2.0, imgWidth, imgHeight)
    }

    // Draw Hexagon Border / Highlight
    val xPoints = DoubleArray(6) { i -> cx + HEX_SIZE * sin(i * PI / 3 + PI / 6) }
    val yPoints = DoubleArray(6) { i -> cy + HEX_SIZE * cos(i * PI / 3 + PI / 6) }

    if (isHighlighted) {
      fill = Color.GOLD
      globalAlpha = 0.4
      fillPolygon(xPoints, yPoints, 6)
      globalAlpha = 1.0

      stroke = Color.GOLD
      lineWidth = 3.0
      strokePolygon(xPoints, yPoints, 6)
      lineWidth = 1.0
    }
  }
}