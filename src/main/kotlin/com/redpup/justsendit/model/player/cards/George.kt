package com.redpup.justsendit.model.player.cards

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.tile.proto.SlopeTile
import com.redpup.justsendit.model.board.tile.proto.TerrainPark
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.proto.Grade

class George(val player: Player) : AbilityHandler {

  private var skillBonus = 0

  override suspend fun onBeforeTurn(gameModel: GameModel) {
    if (player.abilities[0] && (gameModel.clock.turn == 1 || gameModel.clock.turn == gameModel.clock.maxTurn)) {
      skillBonus = 2
    } else {
      skillBonus = 0
    }
    super.onBeforeTurn(gameModel)
  }

  override fun computeBonus(tile: SlopeTile): Int {
    return super.computeBonus(tile) + skillBonus
  }

  override fun onGainPoints(points: Int, gameModel: GameModel) {
    val mountainTile = gameModel.tileMap[player.location!!]
    if (player.abilities[1] && mountainTile != null && mountainTile.hasSlope()) {
      val slope = mountainTile.slope
      if (slope.grade == Grade.GRADE_DOUBLE_BLACK) {
        player.mutate { turn.points += 5 }
      }
      if (slope.hasTerrainPark()) {
        val size = slope.terrainPark.size
        if (size == TerrainPark.Size.SIZE_M || size == TerrainPark.Size.SIZE_L) {
          player.mutate { turn.points += 5 }
        }
      }
    }
    super.onGainPoints(points, gameModel)
  }
}
