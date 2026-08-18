package com.redpup.justsendit.model.skill.calculation

import com.redpup.justsendit.model.board.tile.proto.Hazard
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.MountainTileList
import com.redpup.justsendit.model.supply.proto.Icon
import com.redpup.justsendit.model.supply.proto.icon
import com.redpup.justsendit.util.TextProtoReaderImpl

/** Wrapper on EV operations on [Icon]s. */
object Icons {
  private val tilesList =
    TextProtoReaderImpl<MountainTile, MountainTileList.Builder>(
      "src/main/resources/com/redpup/justsendit/model/board/tile/mountain_tiles.textproto",
      MountainTileList::newBuilder,
      MountainTileList.Builder::getTilesList,
    )

  private val allSlopes = tilesList().filter { it.hasSlope() }.map { it.slope }

  private val frequencyMap = allSlopes
    .flatMap {
      listOf(
        icon { grade = it.grade },
        icon { condition = it.condition },
      ) + it.hazardsList.map { icon { hazard = it } }
    }
    .groupingBy { it }
    .eachCount()
    .mapValues { it.value.toDouble() / allSlopes.size }

  /** Frequency of an [Icon] applying to a tile. */
  val Icon.frequency: Double
    get() = when (typeCase) {
      Icon.TypeCase.WILD -> 1.0
      Icon.TypeCase.TYPE_NOT_SET -> 0.0
      else -> frequencyMap[this] ?: 0.0
    }
}