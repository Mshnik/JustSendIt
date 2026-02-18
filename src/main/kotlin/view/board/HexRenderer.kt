package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.proto.Grade
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Renderer component that renders a single hex on the screen. */
class HexRenderer(private val gc: GraphicsContext, private val hexSize: Double) {

  /** Draws this hex using [gc]. */
  fun draw(tile: MountainTile, cx: Double, cy: Double) {
    // 1. Draw Hexagon Border
    val xPoints = DoubleArray(6) { i -> cx + hexSize * cos(i * PI / 3) }
    val yPoints = DoubleArray(6) { i -> cy + hexSize * sin(i * PI / 3) }

    gc.stroke = Color.LIGHTGREY
    gc.strokePolygon(xPoints, yPoints, 6)

    when (tile.tileCase) {
      MountainTile.TileCase.SLOPE -> drawSlope(tile.slope, cx, cy)
      MountainTile.TileCase.LIFT -> drawLift(tile.lift, cx, cy)
      MountainTile.TileCase.TILE_NOT_SET -> {}
    }

    if (tile.apresLink > 0) {
      drawCircledText(cx, cy + 20, Color.MEDIUMPURPLE, tile.apresLink.toString())
    }
  }

  /** Draws this [slope] using [gc]. */
  private fun drawSlope(slope: SlopeTile, cx: Double, cy: Double) {
    // 2. Draw Grade Symbol (Top)
    when (slope.grade) {
      Grade.GRADE_GREEN -> {
        gc.fill = Color.GREEN
        gc.fillOval(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLUE -> {
        gc.fill = Color.BLUE
        gc.fillRect(cx - 10, cy - 40, 20.0, 20.0)
      }

      Grade.GRADE_BLACK -> {
        drawDiamond(cx, cy - 30, Color.BLACK)
      }

      Grade.GRADE_DOUBLE_BLACK -> {
        drawDiamond(cx - 12, cy - 30, Color.BLACK)
        drawDiamond(cx + 12, cy - 30, Color.BLACK)
      }

      Grade.GRADE_UNSET -> {}
      Grade.UNRECOGNIZED -> {}
    }

    // 3. Draw Difficulty & Condition (Center)
    gc.fill = Color.DARKSLATEGRAY
    gc.fillText("Diff: ${slope.difficulty}", cx - 20, cy)
    gc.fillText(slope.condition.name.removePrefix("CONDITION_"), cx - 25, cy + 15)

    // 4. Draw Hazards (Bottom)
    val hazardText =
      slope.hazardsList.map { it.name.removePrefix("HAZARD_") }.joinToString(",") { it.take(1) }
    if (hazardText.isNotEmpty()) {
      gc.strokeText("⚠️ $hazardText", cx - 20, cy + 35)
    }
  }

  /** Draws this [lift] using [gc]. */
  private fun drawLift(lift: LiftTile, cx: Double, cy: Double) {
    val color = when (lift.color) {
      LiftColor.LIFT_COLOR_UNSET, LiftColor.UNRECOGNIZED -> Color.BLACK
      LiftColor.LIFT_COLOR_CYAN -> Color.CYAN
      LiftColor.LIFT_COLOR_RED -> Color.RED
      LiftColor.LIFT_COLOR_YELLOW -> Color.YELLOW
      LiftColor.LIFT_COLOR_PURPLE -> Color.PURPLE
      LiftColor.LIFT_COLOR_GREY -> Color.GRAY
    }
    drawArrow(cx, cy, color, lift.direction)
  }

  /** Draws a diamond symbol. */
  private fun drawDiamond(cx: Double, cy: Double, color: Color) {
    gc.fill = color
    gc.fillPolygon(
      doubleArrayOf(cx, cx + 10, cx, cx - 10), doubleArrayOf(cy - 10, cy, cy + 10, cy), 4
    )
  }

  /** Draws an arrow symbol. */
  private fun drawArrow(
    cx: Double,
    cy: Double,
    color: Color,
    direction: LiftDirection,
  ) {
    gc.fill = color

    val yInvert = when (direction) {
      LiftDirection.LIFT_DIRECTION_UNSET, LiftDirection.UNRECOGNIZED, LiftDirection.LIFT_DIRECTION_BOTTOM -> 1
      LiftDirection.LIFT_DIRECTION_TOP -> -1
    }

    gc.fillPolygon(
      doubleArrayOf(cx - 15, cx, cx + 15), doubleArrayOf(cy, cy - 20 * yInvert, cy), 3
    )
  }

  /** Draws [text] in a circle. */
  private fun drawCircledText(cx: Double, cy: Double, color: Color, text: String) {
    gc.fill = color
    gc.stroke = color
    gc.strokeOval(cx - 10, cy - 10, 20.0, 20.0)
    gc.fillText(text, cx - 5, cy + 5)
  }
}
