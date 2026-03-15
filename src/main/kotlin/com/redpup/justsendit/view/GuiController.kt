package com.redpup.justsendit.view

import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.tile.proto.MountainTile.TileCase
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.TrainingChip
import com.redpup.justsendit.model.player.proto.mountainDecision
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
  override suspend fun choosePlayerCard(
    player: Player,
    cards: List<com.redpup.justsendit.model.player.cards.PlayerCard>,
  ): com.redpup.justsendit.model.player.cards.PlayerCard {
    TODO("Not yet implemented")
  }

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
            TileCase.SLOPE -> handleSkiRide(it.first, continuation)
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

      "Rest" -> continuation.resume(mountainDecision { rest = empty { } })
      "Lift" -> continuation.resume(mountainDecision { lift = empty { } })
      "Exit" -> continuation.resume(mountainDecision { exit = empty { } })
      "Pass" -> continuation.resume(mountainDecision { pass = empty { } })
      else -> throw IllegalArgumentException()
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

  override suspend fun chooseChipsToUse(
    player: Player,
    tile: com.redpup.justsendit.model.board.tile.proto.SlopeTile,
    currentSkill: Int,
    difficulty: Int,
  ): List<TrainingChip> {
    return emptyList()
  }

  override suspend fun chooseOtherApres(
    player: Player,
    otherApres: List<Apres>,
    count: Int,
  ): List<Apres> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseChipsToGain(player: Player, count: Int): List<TrainingChip> {
    TODO("Not yet implemented")
  }
}
