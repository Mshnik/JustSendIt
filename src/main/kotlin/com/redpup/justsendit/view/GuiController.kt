package com.redpup.justsendit.view

import com.google.common.collect.Range
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
import com.redpup.justsendit.view.board.HexGridViewer
import com.redpup.justsendit.view.player.ActivePlayerArea
import com.redpup.justsendit.view.player.PlayerCardChooser
import com.redpup.justsendit.view.skill.CardInspector
import javafx.application.Platform
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
            it.resetListeners()
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

  override suspend fun chooseMountainTile(
    player: Player,
    elements: Collection<HexPoint>,
  ): HexPoint {
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

    if (decision.hasSkiRide()) {
      val choices = gameModel.getAvailableMoves(player)
      return chooseMountainTile(player, choices.keys)
        .let {
          val direction = choices[it] ?: throw IllegalStateException("Illegal point chosen: $it")
          mountainDecision { skiRide = skiRideDecision { this.direction = direction } }
        }
    }

    // For other decisions, they might be complete or need more info
    if (decision.hasPass()) {
      // TODO: handle card buying in a non-native dialog way
    }

    return decision
  }

  override suspend fun chooseSkiRideResolutionAction(
    player: Player,
    gameModel: GameModel,
  ): SkiRideResolutionAction {
    // TODO: Handle stop case here.
    return chooseSkillCards(
      player,
      player.hand,
      Range.closed(1, 1),
      PlayerController.SkillZone.HAND
    ).let { skiRideResolutionAction { play = playCardAction { cardName = it.first().name } } }
  }
}
