package com.redpup.justsendit.control.player

/** Controller events related to skill cards. */
sealed interface SkillEvent

data object PlaySkillForSkiRideAttempt : SkillEvent
data object PlaySkillForLift : SkillEvent
data object TrashSkill : SkillEvent
data object ChooseCardToBuy : SkillEvent
data object DiscardForCrash : SkillEvent

/** Reasons a player controller can be invoked to choose a mountain tile. */
sealed interface MountainTileEvent

data object ChooseStartOfDayLocation : MountainTileEvent
data object ChooseSkiRideDestination : MountainTileEvent
