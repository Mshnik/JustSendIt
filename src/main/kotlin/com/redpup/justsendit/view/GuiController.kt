package com.redpup.justsendit.view

import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.MutableGameModel
import com.redpup.justsendit.model.board.grid.HexExtensions.isDownMountain
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.MountainTile.TileCase
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.view.board.HexGridViewer
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.TextInputDialog
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class GuiController @Inject constructor() : PlayerController {

  lateinit var hexGridViewer: HexGridViewer
  override val name = "GuiController"

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val choices = mutableListOf<String>()
        if (player.location != null) {
          choices.add("Ski/Ride")
          choices.add("Rest")
          choices.add("Lift")
          choices.add("Exit")
        }
        choices.add("Pass")

        val dialog = ChoiceDialog(choices[0], choices)
        dialog.title = "Choose Action"
        dialog.headerText = "What do you want to do?"
        val result = dialog.showAndWait()
        check(result.isPresent)

        val choice = result.get()
        when (choice) {
          "Ski/Ride" -> {
            val availableMoves = (gameModel as MutableGameModel).getAvailableMoves(player)
            hexGridViewer.highlightHexes(availableMoves.keys)

            hexGridViewer.onHexClicked = { clickedHex ->
              val direction = availableMoves[clickedHex]
              val tile = gameModel.tileMap[clickedHex]
              if (direction != null && tile != null) {
                hexGridViewer.highlightHexes(emptySet())
                hexGridViewer.onHexClicked = null

                when (tile.tileCase) {
                  TileCase.SLOPE -> handleSkiRide(
                    direction,
                    continuation
                  )

                  TileCase.LIFT -> continuation.resume(
                    mountainDecision {
                      skiRide = skiRideDecision {
                        this.direction = direction
                      }
                    })

                  TileCase.TILE_NOT_SET, null -> {}
                }
              }
            }
          }

          "Rest" -> continuation.resume(mountainDecision { rest = empty { } })
          "Lift" -> continuation.resume(mountainDecision { lift = empty { } })
          "Exit" -> continuation.resume(mountainDecision { exit = empty { } })
          "Pass" -> continuation.resume(mountainDecision { pass = empty { } })
          else -> throw IllegalArgumentException()
        }
      }
    }
  }

  private fun handleSkiRide(
    direction: HexDirection,
    continuation: CancellableContinuation<MountainDecision>,
  ) {
    val numCardsDialog = TextInputDialog("1")
    numCardsDialog.title = "Number of Cards"
    numCardsDialog.headerText = "Enter the number of cards to play."
    val numCardsResult = numCardsDialog.showAndWait()
    check(numCardsResult.isPresent)

    val numCards = numCardsResult.get().toIntOrNull() ?: 1
    continuation.resume(mountainDecision {
      skiRide = skiRideDecision {
        this.direction = direction
        this.numCards = numCards
      }
    })
  }

  override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    // For now, return a default location
    return gameModel.tileMap.keys().first { gameModel.tileMap[it]!!.hasLift() }
  }

  override suspend fun chooseCardsToRemove(
    player: Player,
    cards: List<Int>,
    maxToRemove: Int,
  ): List<Int> {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val dialog = TextInputDialog()
        dialog.title = "Choose Cards to Remove"
        dialog.headerText = "Enter card indices to remove (comma-separated), max $maxToRemove"
        dialog.contentText = "Your cards: ${cards.joinToString()}"
        val result = dialog.showAndWait()
        if (result.isPresent) {
          val indices = result.get().split(",").mapNotNull { it.trim().toIntOrNull() }
          continuation.resume(indices)
        } else {
          continuation.resume(emptyList())
        }
      }
    }
  }

  override suspend fun shouldGainSpeed(player: Player): Boolean {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Gain Speed?"
        alert.headerText = "Do you want to gain speed?"
        val result = alert.showAndWait()
        continuation.resume(result.get() == ButtonType.OK)
      }
    }
  }

  override suspend fun chooseMoveOnRest(player: Player): HexDirection? {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val dialog =
          ChoiceDialog<HexDirection>(null, HexDirection.entries.filter { it.isDownMountain })
        dialog.title = "Move on Rest?"
        dialog.headerText = "Choose a direction to move, or cancel."
        val result = dialog.showAndWait()
        if (result.isPresent) {
          continuation.resume(result.get())
        } else {
          continuation.resume(null)
        }
      }
    }
  }

  override suspend fun decideToUseEndurance(): Boolean {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Use Endurance?"
        alert.headerText = "Do you want to use your endurance ability?"
        val result = alert.showAndWait()
        continuation.resume(result.get() == ButtonType.OK)
      }
    }
  }

  override suspend fun onRevealTopCard(card: Int) {
    suspendCancellableCoroutine<Unit> { continuation ->
      Platform.runLater {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Top Card"
        alert.headerText = "The top card of your skill deck is: $card"
        alert.showAndWait()
        continuation.resume(Unit)
      }
    }
  }
}
