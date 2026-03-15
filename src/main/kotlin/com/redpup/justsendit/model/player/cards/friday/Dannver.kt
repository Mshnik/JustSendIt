package com.redpup.justsendit.model.player.cards.friday

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto
import com.redpup.justsendit.model.proto.Grade

class Dannver(override val proto: PlayerCardProto) : PlayerCard {
  private val riddenGrades = mutableSetOf<Grade>()

  override fun startTurn() {
    riddenGrades.clear()
  }

  override fun handleGameEvent(
    event: PlayerGameEvent,
    player: MutablePlayer,
    gameModel: GameModel,
  ) {
    if (event is PlayerGameEvent.PlayerSkiRide) {
      val grade = event.slope.grade
      if (riddenGrades.add(grade)) {
        player.turn.points += 1
      }
    }
  }
}
