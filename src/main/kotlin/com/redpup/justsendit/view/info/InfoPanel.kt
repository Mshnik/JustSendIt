package com.redpup.justsendit.view.info

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.util.toTitleCase
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.VBox

class InfoPanel(private val gameModel: GameModel) : VBox() {

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
    children.addAll(hexInfoVBox, Separator(), playersInfoVBox)
  }

  fun updateHexInfo(tile: MountainTile) {
    hexInfoVBox.children.clear()
    val title = Label("Hex Info")
    title.styleClass.add("section-title")
    hexInfoVBox.children.add(title)

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
    if (players.isNotEmpty()) {
      val title = Label("Players on Hex")
      title.styleClass.add("section-title")
      playersInfoVBox.children.add(title)
      players.forEach { player ->
        val info = StringBuilder()
        info.append("Name: ${player.playerCard.name}\n")
        info.append("Points: ${player.points}\n")
        info.append("Experience: ${player.experience}\n")
        info.append("Training: ${player.training}\n")
        info.append("Abilities: ${player.abilities}\n")
        val playerBox = VBox()
        playerBox.padding = Insets(5.0, 0.0, 5.0, 10.0)
        addInfoLabel(info.toString(), playerBox)
        playersInfoVBox.children.add(playerBox)
      }
    }
  }

  fun clear() {
    hexInfoVBox.children.clear()
    playersInfoVBox.children.clear()
  }

  private fun addInfoLabel(text: String, parent: VBox) {
    val label = Label(text)
    parent.children.add(label)
  }
}
