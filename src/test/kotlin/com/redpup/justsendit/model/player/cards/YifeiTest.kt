package com.redpup.justsendit.model.player.cards

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.Hazard
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

class YifeiTest {

    private lateinit var player: MutablePlayer
    private lateinit var yifei: AbilityHandler
    private lateinit var gameModel: GameModel

    @BeforeEach
    fun setup() {
        val playerCard = playerCard { name = "Yifei" }
        player = MutablePlayer(playerCard, mock()) { p -> Yifei(p) }
        yifei = player.abilityHandler
        gameModel = mock()
    }

    @Test
    fun `getHazardTrainingMultiplier returns 2 if ability unlocked`() {
        player.mutate { abilities[0] = true }
        assertThat(yifei.getHazardTrainingMultiplier()).isEqualTo(2)
    }

    @Test
    fun `onGainPoints adds points if tile has hazards and ability unlocked`() {
        player.mutate { abilities[1] = true }
        val tileMap = HexGrid<MountainTile>()
        val location = hexPoint { q = 0; r = 0 }
        tileMap[location] = mountainTile {
            slope = slopeTile { hazards.add(Hazard.HAZARD_TREES) }
        }
        whenever(gameModel.tileMap).thenReturn(tileMap)
        player.mutate { this.location = location }

        val initialPoints = player.turn.points
        yifei.onGainPoints(5, gameModel)
        assertThat(player.turn.points).isEqualTo(initialPoints + 1)
    }
}
