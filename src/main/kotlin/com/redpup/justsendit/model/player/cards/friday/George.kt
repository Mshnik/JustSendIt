package com.redpup.justsendit.model.player.cards.friday

import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.PlayerGameEvent
import com.redpup.justsendit.model.player.proto.PlayerCard as PlayerCardProto

/**
 *
 */
class George(override val proto: PlayerCardProto) : PlayerCard {
  override fun handleGameEvent(event: PlayerGameEvent) {
    TODO("Not yet implemented")
  }
}