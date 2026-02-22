package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.board.tile.proto.MountainTile
import com.redpup.justsendit.model.board.tile.proto.mountainTile
import com.redpup.justsendit.model.board.tile.proto.slopeTile
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.proto.playerCard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class JamesTest {

    private lateinit var player: MutablePlayer
    private lateinit var james: AbilityHandler
    private lateinit var gameModel: GameModel

    @BeforeEach
    fun setup() {
        val playerCard = playerCard { name = "James" }
        player = MutablePlayer(playerCard, mock()) { p -> James(p) }
        james = player.abilityHandler
        gameModel = mock()
    }

    @Test
    fun `onGainPoints adds points on groomers if ability unlocked`() {
        player.mutate { abilities[1] = true }
        val tileMap = HexGrid<MountainTile>()
        val location = hexPoint { q = 0; r = 0 }
        tileMap[location] = mountainTile {
            slope = slopeTile { condition = Condition.CONDITION_GROOMED }
        }
        whenever(gameModel.tileMap).thenReturn(tileMap)
        player.mutate { this.location = location }

        val initialPoints = player.turn.points
        james.onGainPoints(5, gameModel)
        assertThat(player.turn.points).isEqualTo(initialPoints + 2)
    }

    @Test
    fun `onSuccessfulRun gains speed if diff is 7 or more and ability unlocked`() {
        player.mutate { abilities[0] = true }
        whenever(player.handler.shouldGainSpeed(player)).thenReturn(true)
        val initialSpeed = player.turn.speed
        james.onSuccessfulRun(gameModel, 7)
        assertThat(player.turn.speed).isEqualTo(initialSpeed + 1)
    }
}
