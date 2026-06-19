package com.redpup.justsendit.control

import com.redpup.justsendit.model.board.tile.proto.SlopeTile

/** Controller events related to skill cards. */
sealed interface SkillEvent

data class PlaySkillForSkiRideAttempt(
  val slope: SlopeTile,
  val cumulativeSkill: Int,
  val totalWobbles: Int,
) : SkillEvent

data object PlaySkillForLift : SkillEvent
data object TrashSkill : SkillEvent
data class ChooseCardToBuy(val studyValue: Int) : SkillEvent
data object DiscardForCrash : SkillEvent

/** Reasons a player controller can be invoked to choose a mountain tile. */
sealed interface MountainTileEvent

data object ChooseStartOfDayLocation : MountainTileEvent
data object ChooseSkiRideDestination : MountainTileEvent
