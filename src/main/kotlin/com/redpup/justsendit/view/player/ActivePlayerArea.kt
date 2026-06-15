package com.redpup.justsendit.view.player

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.liftDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.passDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.view.GuiState
import com.redpup.justsendit.view.skill.CardWidget
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.CompletableDeferred

/**
 * Bottom region of the GUI.
 * - Left: Active Player Card & Visual Wobble Node Counter Grid
 * - Center: Phase Chooser Block ---> Active Hand Buffered Row
 * - Right: Card Count Badges for Draw Deck & Discard Piles
 */
class ActivePlayerArea(private val guiState: GuiState) : HBox(), Logger {

  private val leftSection = VBox()
  private val centerSection = VBox()
  private val rightSection = VBox()

  private val phaseChooser = HBox()
  private val handRow = HBox()

  private val skiButton = Button("SKI / RIDE")
  private val liftButton = Button("TAKE LIFT")
  private val passButton = Button("PASS")
  private val confirmButton = Button("CONFIRM")

  private var decisionDeferred: CompletableDeferred<MountainDecision>? = null
  private var confirmDeferred: CompletableDeferred<Unit>? = null

  init {
    this.styleClass.add("active-player-area")
    this.alignment = Pos.CENTER
    this.spacing = 20.0
    this.prefHeight = 200.0

    setupLeft()
    setupCenter()
    setupRight()

    children.addAll(leftSection, centerSection, rightSection)
    setHgrow(centerSection, Priority.ALWAYS)

    setButtonsEnabled(false)
    confirmButton.isDisable = true
  }

  override fun log(log: Log) {
    Platform.runLater { update(guiState.gameModel.currentPlayer) }
  }

  private fun setupLeft() {
    leftSection.prefWidth = 200.0
    leftSection.alignment = Pos.CENTER_LEFT
    leftSection.children.add(Label("Player Card & Wobbles"))
  }

  private fun setupCenter() {
    centerSection.alignment = Pos.CENTER
    centerSection.spacing = 10.0

    phaseChooser.alignment = Pos.CENTER
    phaseChooser.spacing = 15.0

    skiButton.styleClass.add("phase-button")
    liftButton.styleClass.add("phase-button")
    passButton.styleClass.add("phase-button")
    confirmButton.styleClass.add("confirm-button")

    skiButton.setOnAction { completeDecision(mountainDecision { skiRide = skiRideDecision {} }) }
    liftButton.setOnAction { completeDecision(mountainDecision { lift = liftDecision {} }) }
    passButton.setOnAction { completeDecision(mountainDecision { pass = passDecision {} }) }
    confirmButton.setOnAction { confirmDeferred?.complete(Unit) }

    phaseChooser.children.addAll(skiButton, liftButton, passButton, confirmButton)

    handRow.alignment = Pos.CENTER
    handRow.spacing = 6.0
    handRow.prefHeight = 120.0
    handRow.styleClass.add("hand-row")

    centerSection.children.addAll(phaseChooser, handRow)
  }

  private fun completeDecision(decision: MountainDecision) {
    decisionDeferred?.complete(decision)
    setButtonsEnabled(false)
  }

  fun setButtonsEnabled(enabled: Boolean) {
    skiButton.isDisable = !enabled
    liftButton.isDisable = !enabled
    passButton.isDisable = !enabled

    // Reset visual state
    skiButton.styleClass.remove("selected")
    liftButton.styleClass.remove("selected")
    passButton.styleClass.remove("selected")
  }

  suspend fun awaitMountainDecision(): MountainDecision {
    setButtonsEnabled(true)
    decisionDeferred = CompletableDeferred()
    return decisionDeferred!!.await()
  }

  suspend fun awaitConfirm() {
    val deferred = CompletableDeferred<Unit>()
    confirmDeferred = deferred
    deferred.await()
    confirmDeferred = null
    confirmButton.isDisable = true
  }

  fun setConfirmEnabled(enabled: Boolean) {
    confirmButton.isDisable = !enabled
  }

  fun update(player: Player) {
    // Update hand
    handRow.children.clear()
    player.hand.forEach { skill ->
      val cardWidget = CardWidget(skill)
      handRow.children.add(cardWidget)
    }
  }

  /** Returns all card widgets in hand. */
  fun getHandWidgets(): List<CardWidget> = handRow.children.filterIsInstance<CardWidget>()

  private fun setupRight() {
    rightSection.prefWidth = 200.0
    rightSection.alignment = Pos.CENTER_RIGHT
    rightSection.children.add(Label("Decks Info"))
  }
}
