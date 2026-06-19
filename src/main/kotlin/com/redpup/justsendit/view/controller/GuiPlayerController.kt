package com.redpup.justsendit.view.controller

import com.google.common.collect.Range
import com.redpup.justsendit.control.MountainTileEvent
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.control.SkillEvent
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.skill.Skill
import com.redpup.justsendit.view.GuiState
import com.redpup.justsendit.view.board.HexGridViewer
import com.redpup.justsendit.view.player.ActivePlayerArea
import com.redpup.justsendit.view.player.PlayerCardChooser
import com.redpup.justsendit.view.sidebar.SidebarHub
import com.redpup.justsendit.view.skill.CardInspector
import com.redpup.justsendit.view.skill.CardWidget
import javafx.application.Platform
import javafx.scene.input.MouseButton
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class GuiPlayerController @Inject constructor(private val guiState: GuiState) : PlayerController {

  lateinit var hexGridViewer: HexGridViewer
  lateinit var activePlayerArea: ActivePlayerArea
  lateinit var sidebarHub: SidebarHub
  override val name = "GuiController"

  override suspend fun chooseSkillCards(
    gameModel: GameModel,
    player: Player,
    event: SkillEvent,
    elements: List<Skill>,
    count: Range<Int>,
    vararg zones: PlayerController.SkillZone,
  ): List<Skill> {
    return suspendCancellableCoroutine { continuation ->
      Platform.runLater {
        val selected = mutableListOf<Skill>()
        val widgets = mutableListOf<CardWidget>()

        if (zones.contains(PlayerController.SkillZone.HAND)) {
          widgets.addAll(activePlayerArea.getHandWidgets())
        }
        if (zones.contains(PlayerController.SkillZone.SHOP)) {
          widgets.addAll(sidebarHub.shopList.children.filterIsInstance<CardWidget>())
        }

        widgets.filter { it.skill in elements }.forEach { widget ->
          widget.setOnMouseClicked { e ->
            if (e.button == MouseButton.PRIMARY) {
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
            } else if (e.button == MouseButton.SECONDARY) {
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
    gameModel: GameModel,
    player: Player,
    elements: List<Apres>,
    count: Range<Int>,
  ): List<Apres> {
    TODO("Not yet implemented")
  }

  override suspend fun chooseMountainTile(
    gameModel: GameModel,
    player: Player,
    event: MountainTileEvent,
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

  override suspend fun choosePlayerCard(
    gameModel: GameModel,
    player: Player,
    elements: List<PlayerCard>,
  ): PlayerCard {
    return PlayerCardChooser.choose(elements)
  }

  override suspend fun makeMountainDecision(
    gameModel: GameModel,
    player: Player,
  ): MountainDecision = activePlayerArea.awaitMountainDecision()
}
