package com.redpup.justsendit.view

import com.google.common.collect.Range
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.MountainTile.TileCase
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.liftDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.passDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.SkiRideResolutionAction
import com.redpup.justsendit.model.player.proto.SkiRideResolutionActionKt.playCardAction
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.skiRideResolutionAction
import com.redpup.justsendit.util.FunctionExtensions.orElse
import com.redpup.justsendit.util.FunctionExtensions.thenNonNull
import com.redpup.justsendit.view.board.HexGridViewer
import javafx.application.Platform
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
      Platform.runLater { handleMountainDecision(player, gameModel, continuation) }
    }
  }

  private fun handleMountainDecision(
    player: Player,
    gameModel: GameModel,
    continuation: CancellableContinuation<MountainDecision>,
  ) {
    val choices = mutableListOf<String>()
    if (player.location != null) {
      choices.add("Ski/Ride")
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
        val availableMoves = gameModel.getAvailableMoves(player)
        hexGridViewer.highlightHexes(availableMoves.keys)

        hexGridViewer.onHexClicked = { clickedHex: HexPoint ->
          val direction = availableMoves[clickedHex]
          val tile = gameModel.tileMap[clickedHex]
          if (direction != null && tile != null) direction to tile else null
        }.thenNonNull {
          hexGridViewer.highlightHexes(emptySet())
          hexGridViewer.onHexClicked = null
          when (it.second.tileCase) {
            TileCase.SLOPE -> continuation.resume(
              mountainDecision {
                skiRide = skiRideDecision {
                  this.direction = it.first
                }
              })

            TileCase.LIFT -> continuation.resume(
              mountainDecision {
                skiRide = skiRideDecision {
                  this.direction = it.first
                }
              })

            TileCase.TILE_NOT_SET, null -> {}
          }
        }.orElse(Unit)
      }

      "Lift" -> {
        val liftChoices = listOf("Ride Up", "Stay")
        val liftDialog = ChoiceDialog(liftChoices[0], liftChoices)
        liftDialog.title = "Lift Action"
        liftDialog.headerText = "Do you want to ride the lift up or stay?"
        val liftResult = liftDialog.showAndWait()
        val rideUpDecision = liftResult.isPresent && liftResult.get() == "Ride Up"

        continuation.resume(mountainDecision {
          lift = liftDecision {
            if (rideUpDecision) {
              rideUp = empty { }
            } else {
              stay = empty { }
            }
          }
        })
      }

      "Exit" -> continuation.resume(mountainDecision { exit = empty { } })
      "Pass" -> {
        val buyDialog = TextInputDialog()
        buyDialog.title = "Pass Action"
        buyDialog.headerText =
          "Enter the name of the card to buy from the shop (leave empty if none)."
        val buyResult = buyDialog.showAndWait()
        val buyName = if (buyResult.isPresent) buyResult.get() else ""

        continuation.resume(mountainDecision {
          pass = passDecision {
            buyCardName = buyName
          }
        })
      }

      else -> throw IllegalArgumentException()
    }
  }

  override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
    return gameModel.tileMap.keys().first { gameModel.tileMap[it]!!.hasLift() }
  }

  override suspend fun <T> choose(
    player: Player,
    elements: List<T>,
    count: Range<Int>,
  ): List<T> {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val dialog = TextInputDialog()
        // TODO: Metadata based on choose event.
        // dialog.title = "Choose Cards to Remove"
        // dialog.headerText = "Enter card indices to remove (comma-separated), max $maxToRemove"
        // dialog.contentText =
        //   "Your cards: ${cards.mapIndexed { idx, card -> "$idx: ${card.name}" }.joinToString()}"
        val result = dialog.showAndWait()
        if (result.isPresent) {
          val indices = result.get().split(",").mapNotNull { it.trim().toIntOrNull() }
          val selected = indices.filter { it in elements.indices }.map { elements[it] }
            .take(count.lowerEndpoint())
          continuation.resume(selected)
        } else {
          // TODO: bad selection
          // continuation.resume(emptyList())
        }
      }
    }
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val choices = player.hand.map { it.name }.toMutableList()
        choices.add("Stop")

        val dialog = ChoiceDialog(choices[0], choices)
        dialog.title = "Ski/Ride Resolution"
        dialog.headerText = "Choose a card to play or Stop."
        val result = dialog.showAndWait()

        if (result.isPresent && result.get() != "Stop") {
          continuation.resume(skiRideResolutionAction {
            playCardAction {
              cardName = result.get()
            }
          })
        } else {
          continuation.resume(skiRideResolutionAction {
            stop = empty { }
          })
        }
      }
    }
  }
}
