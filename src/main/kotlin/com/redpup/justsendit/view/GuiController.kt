package com.redpup.justsendit.view

import com.google.common.collect.Range
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.SkiRideResolutionAction
import com.redpup.justsendit.model.player.proto.SkiRideResolutionActionKt.playCardAction
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.skiRideResolutionAction
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.util.FunctionExtensions.orElse
import com.redpup.justsendit.util.FunctionExtensions.thenNonNull
import com.redpup.justsendit.view.board.HexGridViewer
import javafx.application.Platform
import javafx.scene.control.ChoiceDialog
import javafx.scene.input.MouseButton
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class GuiController @Inject constructor(private val guiState: GuiState) : PlayerController {

  lateinit var hexGridViewer: HexGridViewer
  lateinit var activePlayerArea: ActivePlayerArea
  override val name = "GuiController"

  override suspend fun chooseSkillCards(
    player: Player,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val selected = mutableListOf<Skill>()
        val widgets = activePlayerArea.getHandWidgets()

        widgets.forEach { widget ->
          widget.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY) {
              if (widget.isSelected) {
                widget.isSelected = false
                selected.remove(widget.skill)
              } else {
                if (selected.size < count.upperEndpoint()) {
                  widget.isSelected = true
                  selected.add(widget.skill)
                }
              }

              // Update badges
              widgets.forEach { w ->
                val index = selected.indexOf(w.skill)
                w.selectionIndex = if (index != -1) index + 1 else null
              }

              activePlayerArea.setConfirmEnabled(count.contains(selected.size))
            } else if (event.button == MouseButton.SECONDARY) {
              CardInspector.inspect(widget.skill)
            }
          }
        }

        guiState.coroutineScope.launch {
          activePlayerArea.awaitConfirm()
          // Cleanup
          widgets.forEach {
            it.isSelected = false
            it.selectionIndex = null
            it.onMouseClicked = null // TODO: restore default right-click
          }
          continuation.resume(selected)
        }
      }
    }
  }

  override suspend fun chooseApresCard(
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseMountainTile(player: Player, elements: List<HexPoint>): HexPoint {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        hexGridViewer.highlightHexes(elements)

        hexGridViewer.onHexClicked = { clicked: HexPoint ->
          hexGridViewer.highlightHexes(emptySet())
          hexGridViewer.onHexClicked = null
          continuation.resume(clicked)
        }
      }
    }
  }

  override suspend fun choosePlayerCard(player: Player, elements: List<PlayerCard>): PlayerCard {
    return PlayerCardChooser.choose(elements)
  }

  override suspend fun makeMountainDecision(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    val decision = activePlayerArea.awaitMountainDecision()

    // If it was ski/ride, we need to handle the direction selection
    if (decision.hasSkiRide()) {
      return handleSkiRideSelection(player, gameModel)
    }

    // For other decisions, they might be complete or need more info
    if (decision.hasPass()) {
      // TODO: handle card buying in a non-native dialog way
    }

    return decision
  }

  private suspend fun handleSkiRideSelection(
    player: Player,
    gameModel: GameModel,
  ): MountainDecision {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val availableMoves = gameModel.getAvailableMoves(player)
        hexGridViewer.highlightHexes(availableMoves.keys)

        hexGridViewer.onHexClicked = { clickedHex: HexPoint ->
          val direction = availableMoves[clickedHex]
          val tile = gameModel.tileMap[clickedHex]
          if (direction != null && tile != null) direction to tile else null
        }.thenNonNull {
          hexGridViewer.highlightHexes(emptySet())
          hexGridViewer.onHexClicked = null
          continuation.resume(
            mountainDecision {
              skiRide = skiRideDecision {
                this.direction = it.first
              }
            })
        }.orElse(Unit)
      }
    }
  }

  private suspend fun foo() {
    // return suspendCancellableCoroutine { continuation ->
    //   Platform.runLater {
    //     val dialog = TextInputDialog()
    //     // TODO: Metadata based on choose event.
    //     // dialog.title = "Choose Cards to Remove"
    //     // dialog.headerText = "Enter card indices to remove (comma-separated), max $maxToRemove"
    //     // dialog.contentText =
    //     //   "Your cards: ${cards.mapIndexed { idx, card -> "$idx: ${card.name}" }.joinToString()}"
    //     val result = dialog.showAndWait()
    //     if (result.isPresent) {
    //       val indices = result.get().split(",").mapNotNull { it.trim().toIntOrNull() }
    //       val selected = indices.filter { it in elements.indices }.map { elements[it] }
    //         .take(count.lowerEndpoint())
    //       continuation.resume(selected)
    //     } else {
    //       // TODO: bad selection
    //       // continuation.resume(emptyList())
    //     }
    //   }
    // }
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
