package com.redpup.justsendit.view.board

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.view.player.PlayerRenderer
import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class GuiBoard(private val gameModel: GameModel) : Canvas() {

  companion object {
    private const val SCALE = 0.15

    private const val BOARD_WIDTH = 6377.0 * SCALE
    private const val BOARD_HEIGHT = 4656.0 * SCALE
    private const val HEX_SIZE = 327 * SCALE // Radius from center to corner
    private const val MARGIN = 667 * SCALE // Extra space on sides.
    private const val X_NUDGE = 67 * SCALE
    private const val Y_NUDGE = 0 * SCALE
  }

  private val playerRenderer: PlayerRenderer
  val hexGridViewer: HexGridViewer
  private val pointsRenderer = PointsRenderer(BOARD_WIDTH)

  private val boardImage =
    Image(javaClass.getResource("/com/redpup/justsendit/img/Board.png")!!.toExternalForm())

  init {
    width = BOARD_WIDTH
    height = BOARD_HEIGHT
    hexGridViewer = HexGridViewer(gameModel, HEX_SIZE, MARGIN, X_NUDGE, Y_NUDGE)
    playerRenderer = PlayerRenderer(graphicsContext2D, HEX_SIZE, MARGIN, X_NUDGE, Y_NUDGE)

    val timer = object : AnimationTimer() {
      override fun handle(now: Long) {
        draw(graphicsContext2D)
      }
    }
    timer.start()

    setOnMouseClicked { event ->
      hexGridViewer.fireEvent(event)
    }
  }

  private fun draw(gc: GraphicsContext) {
    gc.clearRect(0.0, 0.0, width, height)

    gc.drawImage(boardImage, 0.0, 0.0, width, height)
    pointsRenderer.draw(gc, gameModel)
    hexGridViewer.draw(gc)
    playerRenderer.render(gameModel)
  }
}