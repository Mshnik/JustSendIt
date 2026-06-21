package com.redpup.justsendit.view.player

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.view.AdvanceButton
import com.redpup.justsendit.view.GuiState
import com.redpup.justsendit.view.skill.CardWidget
import com.redpup.justsendit.view.skill.DeckPile
import com.redpup.justsendit.view.skill.DiscardPile
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
 * - Left: Active Player Card, Wobbles, and Deck
 * - Center: In-Play Area, Phase Chooser Block, and Active Hand
 * - Right: Discard Pile
 */
class ActivePlayerArea(
  private val guiState: GuiState,
  private val advanceButton: AdvanceButton,
) : HBox(), Logger {

  private val leftSection = VBox()
  private val centerSection = VBox()
  private val rightSection = VBox()

  private val deckPile = DeckPile()
  private val discardPile = DiscardPile()
  private val inPlayRow = HBox()
  private val phaseChooser = HBox()
  private val handRow = HBox()

  private val skiButton = Button("SKI / RIDE")
  private val liftButton = Button("TAKE LIFT")
  private val passButton = Button("PASS")
  private val exitButton = Button("EXIT")

  private var decisionDeferred: CompletableDeferred<MountainDecision>? = null

  init {
    this.styleClass.add("active-player-area")
    this.alignment = Pos.CENTER
    this.spacing = 20.0
    this.prefHeight = 350.0 // Increased for in-play row

    setupLeft()
    setupCenter()
    setupRight()

    children.addAll(leftSection, centerSection, rightSection)
    setHgrow(centerSection, Priority.ALWAYS)

    setButtonsEnabled(false)
  }

  override fun log(log: Log) {
    Platform.runLater { update(guiState.gameModel.currentPlayer) }
  }

  private fun setupLeft() {
    leftSection.prefWidth = 200.0
    leftSection.alignment = Pos.CENTER
    leftSection.spacing = 10.0

    val labels = VBox(Label("Player Card"), Label("Wobbles: 0"))
    labels.alignment = Pos.CENTER

    leftSection.children.addAll(deckPile, labels)
  }

  private fun setupCenter() {
    centerSection.alignment = Pos.CENTER
    centerSection.spacing = 15.0

    inPlayRow.alignment = Pos.CENTER
    inPlayRow.spacing = 10.0
    inPlayRow.prefHeight = 120.0
    inPlayRow.styleClass.add("in-play-row")

    phaseChooser.alignment = Pos.CENTER
    phaseChooser.spacing = 15.0

    skiButton.styleClass.add("phase-button")
    liftButton.styleClass.add("phase-button")
    passButton.styleClass.add("phase-button")
    exitButton.styleClass.add("phase-button")

    skiButton.setOnAction { completeDecision(MountainDecision.DECISION_SKI_RIDE) }
    liftButton.setOnAction { completeDecision(MountainDecision.DECISION_LIFT) }
    passButton.setOnAction { completeDecision(MountainDecision.DECISION_PASS) }
    exitButton.setOnAction { completeDecision(MountainDecision.DECISION_EXIT) }

    phaseChooser.children.addAll(skiButton, liftButton, passButton, exitButton, advanceButton)

    handRow.alignment = Pos.CENTER
    handRow.spacing = 6.0
    handRow.prefHeight = 120.0
    handRow.styleClass.add("hand-row")

    centerSection.children.addAll(inPlayRow, phaseChooser, handRow)
  }

  private fun setupRight() {
    rightSection.prefWidth = 200.0
    rightSection.alignment = Pos.CENTER
    rightSection.children.add(discardPile)
  }

  private fun completeDecision(decision: MountainDecision) {
    decisionDeferred?.complete(decision)
    setButtonsEnabled(false)
  }

  private fun setButtonsEnabled(enabled: Boolean) {
    val location = guiState.gameModel.currentPlayer.location
    val tile = location?.let { guiState.gameModel.tileMap[it] }

    skiButton.isDisable = !enabled || guiState.gameModel.getAvailableMoves(guiState.gameModel.currentPlayer).isEmpty()
    liftButton.isDisable = !enabled || tile?.hasLift() != true
    passButton.isDisable = !enabled
    exitButton.isDisable = !enabled || (tile?.apresLink ?: 0) == 0

    // Reset visual state
    skiButton.styleClass.remove("selected")
    liftButton.styleClass.remove("selected")
    passButton.styleClass.remove("selected")
    exitButton.styleClass.remove("selected")
  }

  suspend fun awaitMountainDecision(): MountainDecision {
    setButtonsEnabled(true)
    decisionDeferred = CompletableDeferred()
    return decisionDeferred!!.await()
  }

  suspend fun awaitConfirm(isEmptyAcceptable: Boolean) {
    val deferred = CompletableDeferred<Unit>()
    advanceButton.setupConfirm(deferred, isEmptyAcceptable)
    deferred.await()
  }

  fun setConfirmEnabled(enabled: Boolean) {
    advanceButton.isDisable = !enabled
  }

  fun update(player: Player) {
    // Update labels
    val labels = (leftSection.children[1] as VBox)
    (labels.children[1] as Label).text = "Wobbles: ${player.wobbles}"

    // Update piles
    deckPile.update(player)
    discardPile.update(player)

    // Update in-play
    inPlayRow.children.clear()
    player.inPlay.forEach { skill ->
      val cardWidget = CardWidget(skill, isInPlay = true)
      inPlayRow.children.add(cardWidget)
    }

    // Update hand
    handRow.children.clear()
    player.hand.forEach { skill ->
      val cardWidget = CardWidget(skill)
      handRow.children.add(cardWidget)
    }
  }

  /** Returns all card widgets in hand. */
  fun getHandWidgets(): List<CardWidget> = handRow.children.filterIsInstance<CardWidget>()
}
