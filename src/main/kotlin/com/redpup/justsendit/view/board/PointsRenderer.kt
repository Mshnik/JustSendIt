package com.redpup.justsendit.view.board

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

/** Drawing component that draws player points on the board. */
class PointsRenderer(private val boardWidth: Double, private val boardHeight: Double) : Canvas() {
  private companion object {
    const val MIN_POINTS = 0
    const val MAX_POINTS = 50

    const val TOP_LEFT_POINTS = 18
    const val COLUMNS = 28
    const val TOP_RIGHT_POINTS = TOP_LEFT_POINTS + COLUMNS - 1
  }

  private val boardMargin = boardWidth * 0.0175
  private val pointSize = (boardWidth - boardMargin * 2) / COLUMNS
  private val pointSpacing = pointSize * 0.1

  fun draw(gc: GraphicsContext) {
    for (value in MIN_POINTS..MAX_POINTS) {
      val position = getPointPosition(value)
      gc.fill = Color.GOLD
      gc.fillOval(
        position.first + pointSpacing / 2,
        position.second,
        pointSize - pointSpacing,
        pointSize - pointSpacing
      )
    }
  }

  /** Returns the (x, y) position of the given point value. */
  private fun getPointPosition(value: Int): Pair<Double, Double> {
    check(value in MIN_POINTS..MAX_POINTS) {
      "Expected value in range [$MIN_POINTS,$MAX_POINTS], found $value"
    }

    val x = (value - TOP_LEFT_POINTS).coerceIn(0, COLUMNS - 1)
    val y =
      if (value <= TOP_LEFT_POINTS) TOP_LEFT_POINTS - value
      else if (value >= TOP_RIGHT_POINTS) value - TOP_RIGHT_POINTS
      else 0

    return x.toPointPosition() to y.toPointPosition()
  }

  private fun Int.toPointPosition() = this * pointSize + boardMargin

  /** Returns the (x, y) position of the given bonus point value. */
  private fun getBonusPointPosition(value: Int) {
    check(value % 100 == 0) { "Expected increment of 100, found $value" }
    check(value in 100..500) { "Expected value in range [100,50], found $value" }

  }
}