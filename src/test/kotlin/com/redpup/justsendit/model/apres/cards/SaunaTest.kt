package com.redpup.justsendit.model.apres.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.trainingChip
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SaunaTest {

  private lateinit var player: MutablePlayer
  private val handler: PlayerController = mock()
  private val gameModel: GameModel = mock()

  @BeforeEach
  fun setUp() {
    player = MutablePlayer(handler)
  }

  private val sauna = Sauna(apresCard { name = "Sauna" })

  @Test
  fun `first player gets points for unique chips`() {
    player.trainingChips.add(trainingChip { condition = Condition.CONDITION_GROOMED })
    player.usedTrainingChips.add(trainingChip { condition = Condition.CONDITION_POWDER })
    player.usedTrainingChips.add(trainingChip {
      condition = Condition.CONDITION_POWDER
    }) // duplicate
    runBlocking { sauna.apply(player, true, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(12) // 2 unique * 6
  }

  @Test
  fun `other player gets points for unique chips`() {
    player.trainingChips.add(trainingChip { condition = Condition.CONDITION_GROOMED })
    player.usedTrainingChips.add(trainingChip { condition = Condition.CONDITION_POWDER })
    player.usedTrainingChips.add(trainingChip {
      condition = Condition.CONDITION_POWDER
    }) // duplicate
    runBlocking { sauna.apply(player, false, gameModel) }
    assertThat(player.day.apresPoints).isEqualTo(6) // 2 unique * 3
  }
}
