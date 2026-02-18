package com.redpup.justsendit.view.info

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.player.Player
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class InfoPanel(private val gameModel: GameModel) : VBox() {

    private val hexInfoLabel = Label()
    private val playerInfoLabel = Label()

    init {
        children.addAll(hexInfoLabel, playerInfoLabel)
    }

    fun updateHexInfo(tile: MountainTile) {
        val info = StringBuilder("Hex Info:\n")
        if (tile.hasSlope()) {
            info.append("  Slope:\n")
            info.append("    Grade: ${tile.slope.grade}\n")
            info.append("    Condition: ${tile.slope.condition}\n")
            info.append("    Hazards: ${tile.slope.hazardsList}\n")
        }
        if (tile.hasLift()) {
            info.append("  Lift:\n")
            info.append("    Color: ${tile.lift.color}\n")
            info.append("    Direction: ${tile.lift.direction}\n")
        }
        hexInfoLabel.text = info.toString()
    }

    fun updatePlayerInfo(player: Player) {
        val info = StringBuilder("Player Info:\n")
        info.append("  Name: ${player.playerCard.name}\n")
        info.append("  Points: ${player.points}\n")
        info.append("  Experience: ${player.experience}\n")
        info.append("  Location: ${player.location}\n")
        playerInfoLabel.text = info.toString()
    }

    fun clear() {
        hexInfoLabel.text = ""
        playerInfoLabel.text = ""
    }
}
