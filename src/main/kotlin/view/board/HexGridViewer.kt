package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMap.toMap
import com.redpup.justsendit.model.board.tile.TileReaderImpl
import com.redpup.justsendit.model.board.tile.proto.Grade
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class HexGridViewer : Application() {
  private val hexSize = 60.0 // Radius from center to corner
  private lateinit var tileMap: HexGrid<MountainTile>

  override fun init() {
    tileMap = TileReaderImpl(
      "src/main/resources/model/board/tile/tiles.textproto",
      "src/main/resources/model/board/tile/tile_locations.textproto"
    ).toMap()
  }

  override fun start(stage: Stage) {
    val canvas =
      Canvas((tileMap.width() + 1) * hexSize * 1.5, (tileMap.height() + 1) * hexSize * sqrt(3.0))
    val gc = canvas.graphicsContext2D
    drawGrid(gc)

    stage.scene = Scene(StackPane(canvas))
    stage.title = "Ski Map Hex Grid"
    stage.show()
  }

  private fun drawGrid(gc: GraphicsContext) {
    val offsetX = 100.0
    val offsetY = 100.0

    tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = hexSize * 1.5 * pt.q + offsetX
      val y = hexSize * sqrt(3.0) * (pt.r + pt.q / 2.0) + offsetY

      val tile = tileMap[pt]!!
      drawHexagon(gc, x, y, tile.slope)
    }
  }

  private fun drawHexagon(gc: GraphicsContext, cx: Double, cy: Double, tile: SlopeTile) {
    // 1. Draw Hexagon Border
    val xPoints = DoubleArray(6) { i -> cx + hexSize * cos(i * PI / 3) }
    val yPoints = DoubleArray(6) { i -> cy + hexSize * sin(i * PI / 3) }

    gc.stroke = Color.LIGHTGREY
    gc.strokePolygon(xPoints, yPoints, 6)

    // 2. Draw Grade Symbol (Top)
    when (tile.grade) {
      Grade.GRADE_GREEN -> {
        gc.fill = Color.GREEN; gc.fillOval(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLUE -> {
        gc.fill = Color.BLUE; gc.fillRect(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLACK -> drawDiamond(gc, cx, cy - 30, Color.BLACK)
      Grade.GRADE_DOUBLE_BLACK -> {
        drawDiamond(gc, cx - 12, cy - 30, Color.BLACK)
        drawDiamond(gc, cx + 12, cy - 30, Color.BLACK)
      }

      Grade.GRADE_UNSET -> {}
      Grade.UNRECOGNIZED -> {}
    }

    // 3. Draw Difficulty & Condition (Center)
    gc.fill = Color.DARKSLATEGRAY
    gc.fillText("Diff: ${tile.difficulty}", cx - 20, cy)
    gc.fillText(tile.condition.name.removePrefix("CONDITION_"), cx - 25, cy + 15)

    // 4. Draw Hazards (Bottom)
    val hazardText =
      tile.hazardsList.map { it.name.removePrefix("HAZARD_") }.joinToString(",") { it.take(1) }
    if (hazardText.isNotEmpty()) {
      gc.strokeText("⚠️ $hazardText", cx - 20, cy + 35)
    }
  }

  private fun drawDiamond(gc: GraphicsContext, cx: Double, cy: Double, color: Color) {
    gc.fill = color
    gc.fillPolygon(
      doubleArrayOf(cx, cx + 10, cx, cx - 10), doubleArrayOf(cy - 10, cy, cy + 10, cy), 4
    )
  }
}

fun main() {
  Application.launch(HexGridViewer::class.java)
}