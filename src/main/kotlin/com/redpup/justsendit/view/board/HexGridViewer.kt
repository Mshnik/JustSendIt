package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.Bounds
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

/** Renderer class for drawing the mountain board of hexes. */
class HexGridViewer(
  private val gameModel: GameModel,
  private val hexSize: Double,
  private val margin: Double,
  private val xNudge: Double,
  private val yNudge: Double,
) : Canvas() {
  private val bounds: Bounds = gameModel.tileMap.bounds()
  private var highlightedHexes: Collection<HexPoint> = setOf()
  var onHexClicked: ((HexPoint) -> Unit)? = null

  private val tileImageCache = mutableMapOf<String, Image>()

  init {
    setOnMouseClicked { event ->
      val hex = hexFromPixel(event.x, event.y)
      onHexClicked?.invoke(hex)
    }
  }

  fun highlightHexes(hexes: Collection<HexPoint>) {
    this.highlightedHexes = hexes
  }

  fun draw(gc: GraphicsContext) {
    gc.clearRect(0.0, 0.0, width, height)

    gameModel.tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = xNudge + hexSize * (pt.toX() - bounds.minX) + margin
      val y = yNudge + hexSize * (pt.toY() - bounds.minY) + margin

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

  /** Draws the given [image] at [cx], [cy]. */
  private fun GraphicsContext.draw(image: Image?, cx: Double, cy: Double, isHighlighted: Boolean) {
    if (image != null) {
      // Draw the tile image centered at cx, cy
      // For flat-top hexes, width is 2*R, height is sqrt(3)*R
      val imgWidth = hexSize * 2.0
      val imgHeight = hexSize * 1.73205
      drawImage(image, cx - imgWidth / 2.0, cy - imgHeight / 2.0, imgWidth, imgHeight)
    }

    // Draw Hexagon Border / Highlight
    val xPoints = DoubleArray(6) { i -> cx + hexSize * sin(i * PI / 3 + PI / 6) }
    val yPoints = DoubleArray(6) { i -> cy + hexSize * cos(i * PI / 3 + PI / 6) }

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