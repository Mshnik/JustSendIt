package com.redpup.justsendit.view.info

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.util.toTitleCase
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class InfoPanel(private val gameModel: GameModel) : VBox() {

  private val hexInfoLabel = Label()
  private val playersInfoVBox = VBox() // Changed to VBox to hold multiple player labels

  init {
    this.prefWidth = 200.0 // Set preferred width for constant size
    children.addAll(hexInfoLabel, playersInfoVBox)
  }

  fun updateHexInfo(tile: MountainTile) {
    val info = StringBuilder("Hex Info:\n")
    if (tile.hasSlope()) {
      info.append("  Slope:\n")
      info.append("    Grade: ${tile.slope.grade.name.removePrefix("GRADE_").toTitleCase()}\n")
      info.append(
        "    Condition: ${
          tile.slope.condition.name.removePrefix("CONDITION_").toTitleCase()
        }\n"
      )
      info.append(
        "    Hazards: ${
          tile.slope.hazardsList.map {
            it.name.removePrefix("HAZARD_").toTitleCase()
          }
        }\n"
      )
    }
    if (tile.hasLift()) {
      info.append("  Lift:\n")
      info.append("    Color: ${tile.lift.color.name.removePrefix("LIFT_COLOR_").toTitleCase()}\n")
      info.append(
        "    Direction: ${
          tile.lift.direction.name.removePrefix("LIFT_DIRECTION_").toTitleCase()
        }\n"
      )
    }
    hexInfoLabel.text = info.toString()
  }

  fun updatePlayersInfo(players: List<Player>) {
    playersInfoVBox.children.clear() // Clear previous player info
    if (players.isNotEmpty()) {
      playersInfoVBox.children.add(Label("Players on Hex:"))
      players.forEach { player ->
        val info = StringBuilder()
        info.append("  Name: ${player.playerCard.name}\n")
        info.append("  Points: ${player.points}\n")
        info.append("  Experience: ${player.experience}\n")
        playersInfoVBox.children.add(Label(info.toString()))
      }
    }
  }

  fun clear() {
    hexInfoLabel.text = ""
    playersInfoVBox.children.clear() // Clear all player info
  }
}
