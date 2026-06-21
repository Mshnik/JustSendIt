package com.redpup.justsendit.view.sidebar

import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.util.toTitleCase
import com.redpup.justsendit.view.GuiExt.withStyle
import com.redpup.justsendit.view.GuiState
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.VBox

class InfoPanel(private val guiState: GuiState) : VBox() {

  private val gameInfoVBox = VBox()
  private val hexInfoVBox = VBox()
  private val playersInfoVBox = VBox()

  init {
    this.prefWidth = 250.0
    this.styleClass.add("info-panel")
    this.spacing = 10.0
    hexInfoVBox.spacing = 5.0
    playersInfoVBox.spacing = 10.0
    stylesheets.add(
      javaClass.getResource("/com/redpup/justsendit/view/info/style.css")!!.toExternalForm()
    )
    children.addAll(
      Label("Game Info").withStyle("section-title"),
      gameInfoVBox,
      Separator(),
      Label("Hex Info").withStyle("section-title"),
      hexInfoVBox,
      Separator(),
      Label("Players on Hex").withStyle("section-title"),
      playersInfoVBox
    )
  }

  /** Updates the game info from [guiState]. */
  fun updateGameInfo() {
    gameInfoVBox.children.clear()

    val clock = guiState.gameModel.clock
    addInfoLabel("State: ${clock.state}", gameInfoVBox)
    addInfoLabel("Day: ${clock.day}", gameInfoVBox)
    addInfoLabel("Round: ${clock.round}", gameInfoVBox)
    addInfoLabel("Turn: ${clock.turn}", gameInfoVBox)
    addInfoLabel("Subturn: ${clock.subTurn}", gameInfoVBox)
  }

  /** Updates the [hexInfoVBox] for the given [tile] info. */
  fun updateHexInfo(tile: MountainTile?) {
    if (tile == null) {
      hexInfoVBox.children.clear()
      return
    }

    hexInfoVBox.children.clear()

    if (tile.hasSlope()) {
      val info = StringBuilder()
      info.append("Difficulty: ${tile.slope.difficulty}\n")
      info.append("Grade: ${tile.slope.grade.name.removePrefix("GRADE_").toTitleCase()}\n")
      info.append(
        "Condition: ${
          tile.slope.condition.name.removePrefix("CONDITION_").toTitleCase()
        }\n"
      )
      if (tile.slope.hazardsList.isNotEmpty()) {
        info.append(
          "Hazards: ${
            tile.slope.hazardsList.joinToString(", ") {
              it.name.removePrefix("HAZARD_").toTitleCase()
            }
          }\n"
        )
      }
      addInfoLabel(info.toString(), hexInfoVBox)
    }
    if (tile.hasLift()) {
      val info = StringBuilder()
      info.append("Lift Color: ${tile.lift.color.name.removePrefix("LIFT_COLOR_").toTitleCase()}\n")
      info.append(
        "Lift Direction: ${
          tile.lift.direction.name.removePrefix("LIFT_DIRECTION_").toTitleCase()
        }\n"
      )
      addInfoLabel(info.toString(), hexInfoVBox)
    }
  }

  fun updatePlayersInfo(players: List<Player>) {
    playersInfoVBox.children.clear()
    players.forEach { player ->
      val info = StringBuilder()
      info.append("Name: ${player.name}\n")
      info.append("Points: ${player.points}\n")
      info.append("Wobbles: ${player.wobbles}\n")
      val playerBox = VBox()
      playerBox.padding = Insets(5.0, 0.0, 5.0, 10.0)
      addInfoLabel(info.toString(), playerBox)
      playersInfoVBox.children.add(playerBox)
    }
  }

  private fun addInfoLabel(text: String, parent: VBox) {
    val label = Label(text)
    parent.children.add(label)
  }
}
