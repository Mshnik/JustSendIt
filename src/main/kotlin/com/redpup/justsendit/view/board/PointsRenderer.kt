package com.redpup.justsendit.view.board

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

/** Drawing component that draws player points on the board.
 *
 * The series of somewhat arbitrary constants in this file align drawings with the point spots
 * on the board image. Changes in the Figma constants like board size, point space margin, etc
 * will change these.
 */
class PointsRenderer(private val boardWidth: Double) : Canvas() {
  private companion object {
    const val MIN_POINTS = 0
    const val MAX_POINTS = 50

    const val MIN_BONUS_POINTS = 100
    const val MAX_BONUS_POINTS = 500

    const val TOP_LEFT_POINTS = 18
    const val COLUMNS = 28
    const val TOP_RIGHT_POINTS = TOP_LEFT_POINTS + COLUMNS - 1
  }

  private val boardMargin = boardWidth * 0.0175
  private val combinedPointSize = (boardWidth - boardMargin * 2) / COLUMNS
  private val pointSize = combinedPointSize * 10 / 11
  private val pointSpacing = pointSize * 0.1
  private val bonusPointSize = pointSize * 1.5
  private val bonusPointSpacing = bonusPointSize * 275 / 300

  fun draw(gc: GraphicsContext) {
    for (value in MIN_POINTS..MAX_POINTS) {
      val position = getPointPosition(value)
      gc.fill = Color.GOLD
      gc.fillOval(
        position.first + pointSpacing / 2,
        position.second,
        pointSize,
        pointSize
      )
    }

    for (value in MIN_BONUS_POINTS..MAX_BONUS_POINTS step 100) {
      val position = getBonusPointPosition(value)
      gc.fill = Color.GOLD
      gc.fillOval(
        position.first,
        position.second,
        bonusPointSize,
        bonusPointSize
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

  private fun Int.toPointPosition() = this * (pointSize + pointSpacing) + boardMargin

  /** Returns the (x, y) position of the given bonus point value. */
  private fun getBonusPointPosition(value: Int): Pair<Double, Double> {
    check(value % 100 == 0) { "Expected increment of 100, found $value" }
    check(value in MIN_BONUS_POINTS..MAX_BONUS_POINTS) {
      "Expected value in range [$MIN_BONUS_POINTS,$MAX_BONUS_POINTS], found $value"
    }

    val x = boardWidth - bonusPointSize - boardMargin
    val index = (value / 100) - 1
    val y =
      index * (bonusPointSize + bonusPointSpacing) +
        bonusPointSize * 1.4 +
        getPointPosition(50).second

    return x to y
  }
}