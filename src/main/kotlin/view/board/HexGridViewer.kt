package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.board.grid.Bounds
import com.redpup.justsendit.model.board.grid.HexExtensions.toX
import com.redpup.justsendit.model.board.grid.HexExtensions.toY
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMap.toMap
import com.redpup.justsendit.model.board.tile.TileReaderImpl
import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.proto.Grade
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

class HexGridViewer : Application() {
  private val hexSize = 60.0 // Radius from center to corner
  private val margin = 60.0 // Extra space on sides.
  private lateinit var tileMap: HexGrid<MountainTile>
  private lateinit var bounds: Bounds

  override fun init() {
    tileMap = TileReaderImpl(
      "src/main/resources/model/board/tile/tiles.textproto",
      "src/main/resources/model/board/tile/tile_locations.textproto"
    ).toMap()
    bounds = tileMap.bounds()
  }

  override fun start(stage: Stage) {
    val canvas = Canvas(bounds.width * hexSize + margin, bounds.height * hexSize + margin)
    val gc = canvas.graphicsContext2D
    drawGrid(gc)

    stage.scene = Scene(StackPane(canvas))
    stage.title = "Ski Map Hex Grid"
    stage.show()
  }

  private fun drawGrid(gc: GraphicsContext) {
    tileMap.keys().forEach { pt ->
      // Axial to Pixel conversion for Flat-Top
      val x = hexSize * (pt.toX() - bounds.minX) + margin
      val y = hexSize * (pt.toY() - bounds.minY) + margin

      tileMap[pt]!!.draw(gc, x, y)
    }
  }

  private fun MountainTile.draw(gc: GraphicsContext, cx: Double, cy: Double) {
    // 1. Draw Hexagon Border
    val xPoints = DoubleArray(6) { i -> cx + hexSize * cos(i * PI / 3) }
    val yPoints = DoubleArray(6) { i -> cy + hexSize * sin(i * PI / 3) }

    gc.stroke = Color.LIGHTGREY
    gc.strokePolygon(xPoints, yPoints, 6)

    when (tileCase) {
      MountainTile.TileCase.SLOPE -> slope.draw(gc, cx, cy)
      MountainTile.TileCase.LIFT -> lift.draw(gc, cx, cy)
      MountainTile.TileCase.TILE_NOT_SET -> {}
    }

    if (apresLink > 0) {
      gc.drawCircledText(cx, cy + 20, Color.MEDIUMPURPLE, apresLink.toString())
    }
  }

  private fun SlopeTile.draw(gc: GraphicsContext, cx: Double, cy: Double) {
    // 2. Draw Grade Symbol (Top)
    when (grade) {
      Grade.GRADE_GREEN -> {
        gc.fill = Color.GREEN
        gc.fillOval(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLUE -> {
        gc.fill = Color.BLUE
        gc.fillRect(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLACK -> {
        gc.drawDiamond(cx, cy - 30, Color.BLACK)
      }

      Grade.GRADE_DOUBLE_BLACK -> {
        gc.drawDiamond(cx - 12, cy - 30, Color.BLACK)
        gc.drawDiamond(cx + 12, cy - 30, Color.BLACK)
      }

      Grade.GRADE_UNSET -> {}
      Grade.UNRECOGNIZED -> {}
    }

    // 3. Draw Difficulty & Condition (Center)
    gc.fill = Color.DARKSLATEGRAY
    gc.fillText("Diff: $difficulty", cx - 20, cy)
    gc.fillText(condition.name.removePrefix("CONDITION_"), cx - 25, cy + 15)

    // 4. Draw Hazards (Bottom)
    val hazardText =
      hazardsList.map { it.name.removePrefix("HAZARD_") }.joinToString(",") { it.take(1) }
    if (hazardText.isNotEmpty()) {
      gc.strokeText("⚠️ $hazardText", cx - 20, cy + 35)
    }
  }

  private fun LiftTile.draw(gc: GraphicsContext, cx: Double, cy: Double) {
    val color = when (color) {
      LiftColor.LIFT_COLOR_UNSET, LiftColor.UNRECOGNIZED -> Color.BLACK
      LiftColor.LIFT_COLOR_CYAN -> Color.CYAN
      LiftColor.LIFT_COLOR_RED -> Color.RED
      LiftColor.LIFT_COLOR_YELLOW -> Color.YELLOW
      LiftColor.LIFT_COLOR_PURPLE -> Color.PURPLE
      LiftColor.LIFT_COLOR_GREY -> Color.GRAY
    }
    gc.drawArrow(cx, cy, color, direction)
  }

  private fun GraphicsContext.drawDiamond(cx: Double, cy: Double, color: Color) {
    fill = color
    fillPolygon(
      doubleArrayOf(cx, cx + 10, cx, cx - 10), doubleArrayOf(cy - 10, cy, cy + 10, cy), 4
    )
  }

  private fun GraphicsContext.drawArrow(
    cx: Double,
    cy: Double,
    color: Color,
    direction: LiftDirection,
  ) {
    fill = color

    val yInvert = when (direction) {
      LiftDirection.LIFT_DIRECTION_UNSET, LiftDirection.UNRECOGNIZED, LiftDirection.LIFT_DIRECTION_BOTTOM -> 1
      LiftDirection.LIFT_DIRECTION_TOP -> -1
    }

    fillPolygon(
      doubleArrayOf(cx - 15, cx, cx + 15), doubleArrayOf(cy, cy - 20 * yInvert, cy), 3
    )
  }

  private fun GraphicsContext.drawCircledText(cx: Double, cy: Double, color: Color, text: String) {
    fill = color
    stroke = color
    strokeOval(cx - 10, cy - 10, 20.0, 20.0)
    fillText(text, cx - 5, cy + 5)
  }
}

fun main() {
  Application.launch(HexGridViewer::class.java)
}