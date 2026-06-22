package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.view.player.PlayerColors.getPlayerColor
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

  fun draw(gc: GraphicsContext, gameModel: GameModel) {
    for (player in gameModel.players) {
      gc.fill = gameModel.getPlayerColor(player)
      gc.stroke = Color.GREY
      gc.lineWidth = 5.0

      getPointPosition(player.points).let {
        gc.fillDisc(
          it.first + pointSpacing / 2,
          it.second,
          pointSize * 0.9,
        )
      }

      getBonusPointPosition(player.points)?.let {
        gc.fillDisc(
          it.first,
          it.second,
          bonusPointSize * 0.9,
        )
      }
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
  private fun getBonusPointPosition(value: Int): Pair<Double, Double>? {
    if (value < MIN_BONUS_POINTS) {
      return null
    }
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

  /** Draws a disc on a spot. */
  private fun GraphicsContext.fillDisc(x: Double, y: Double, width: Double) {
    val height = width * 0.2
    val squish = width * 0.6
    val skew = width * 0.05
    val shift = width * 0.1

    strokeOval(x, y - squish + width - shift, width, squish)
    fillOval(x, y - squish + width - shift, width, squish)

    strokeOval(x - skew, y - squish + width - height - shift, width, squish)
    fillOval(x - skew, y - squish + width - height - shift, width, squish)
  }
}