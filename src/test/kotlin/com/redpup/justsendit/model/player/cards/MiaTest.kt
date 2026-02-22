package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MiaTest {

    private lateinit var player: MutablePlayer
    private lateinit var mia: AbilityHandler
    private lateinit var gameModel: GameModel

    @BeforeEach
    fun setup() {
        val playerCard = playerCard { name = "Mia" }
        player = MutablePlayer(playerCard, mock()) { p -> Mia(p) }
        mia = player.abilityHandler
        gameModel = mock()
    }

    @Test
    fun `getGreenTrainingBonusGrades returns blue and black if ability unlocked`() {
        player.mutate { abilities[0] = true }
        val grades = mia.getGreenTrainingBonusGrades()
        assertThat(grades).containsExactly(Grade.GRADE_BLUE, Grade.GRADE_BLACK)
    }

    @Test
    fun `onAfterTurn adds points for speed if ability unlocked`() {
        player.mutate { abilities[1] = true }
        player.mutate { turn.speed = 3 }
        val initialPoints = player.turn.points
        mia.onAfterTurn(gameModel)
        assertThat(player.turn.points).isEqualTo(initialPoints + 3)
    }
}
