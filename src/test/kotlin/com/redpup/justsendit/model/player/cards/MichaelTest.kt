package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.proto.Grade
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MichaelTest {

    private lateinit var player: MutablePlayer
    private lateinit var michael: AbilityHandler
    private lateinit var gameModel: GameModel

    @BeforeEach
    fun setup() {
        val playerCard = playerCard { name = "Michael" }
        player = MutablePlayer(playerCard, mock()) { p -> Michael(p) }
        michael = player.abilityHandler
        gameModel = mock()
    }

    @Test
    fun `onBeforeTurn reveals top card if ability unlocked`() {
        player.mutate {
            abilities[0] = true
            skillDeck.clear()
            skillDeck.add(5)
        }
        michael.onBeforeTurn(gameModel)
        verify(player.handler).onRevealTopCard(5)
    }

    @Test
    fun `onGainPoints adds points for unique grade if ability unlocked`() {
        player.mutate { abilities[1] = true }
        val tileMap = HexGrid<MountainTile>()
        val location = hexPoint { q = 0; r = 0 }
        tileMap[location] = mountainTile {
            slope = slopeTile { grade = Grade.GRADE_BLUE }
        }
        whenever(gameModel.tileMap).thenReturn(tileMap)
        player.mutate { this.location = location }

        val initialPoints = player.turn.points
        michael.onGainPoints(5, gameModel)
        assertThat(player.turn.points).isEqualTo(initialPoints + 2)
    }
}
