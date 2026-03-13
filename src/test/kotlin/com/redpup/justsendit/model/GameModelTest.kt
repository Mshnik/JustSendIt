package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.grid.HexGrid
import com.redpup.justsendit.model.board.tile.TileMapBuilder
import com.redpup.justsendit.model.board.tile.proto.Condition
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.PlayerFactory
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.proto.trainingChip
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.util.TimeSource
import com.redpup.justsendit.util.testing.FakeTimeSource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class GameModelTest {

  private val tileMapBuilder: TileMapBuilder = mock()
  private val playerController1: PlayerController = mock()
  private val playerController2: PlayerController = mock()
  private val playerFactory: PlayerFactory = mock()
  private val playerDeck: PlayerDeck = mock()
  private val apresDeck: ApresDeck = mock()
  private val skillDecks: SkillDecks = mock()
  private val timeSource: TimeSource = FakeTimeSource()
  private val logger: Logger = mock()

  private lateinit var player1: MutablePlayer
  private lateinit var player2: MutablePlayer

  private lateinit var gameModel: MutableGameModel

  @BeforeEach
  fun setup() {
    player1 = spy(MutablePlayer(playerController1))
    player2 = spy(MutablePlayer(playerController2))

    whenever(playerFactory.create(playerController1)).thenReturn(player1)
    whenever(playerFactory.create(playerController2)).thenReturn(player2)

    whenever(playerController1.name).thenReturn("PlayerController1")
    whenever(playerController2.name).thenReturn("PlayerController2")

    whenever(tileMapBuilder.build()).thenReturn(HexGrid())

    gameModel = MutableGameModel(
      tileMapBuilder,
      listOf(playerController1, playerController2),
      playerFactory,
      playerDeck,
      apresDeck,
      skillDecks,
      timeSource,
      setOf(logger)
    )
  }

  @Test
  fun `mutate applies function to game model`() {
    gameModel.mutate {
      // Test that we can access mutable properties
      val firstPlayer = players.first()
      firstPlayer.mutate { location = createHexPoint(1, 1) }
    }
    assertThat(gameModel.players.first().location).isEqualTo(createHexPoint(1, 1))
  }

  @Test
  fun `startDay initializes players and apres`() = runBlocking {
    val playerCard1 = playerCard {
      skillCards += Grade.GRADE_GREEN
      chips += trainingChip { condition = Condition.CONDITION_POWDER }
    }
    val playerCard2 = playerCard {
      skillCards += Grade.GRADE_BLUE
      chips += trainingChip { condition = Condition.CONDITION_GROOMED }
    }

    whenever(playerDeck.draw(any(), any())).thenReturn(mutableListOf(playerCard1, playerCard2))
    whenever(playerController1.getStartingLocation(any(), any())).thenReturn(mock())
    whenever(playerController2.getStartingLocation(any(), any())).thenReturn(mock())
    whenever(playerController1.choosePlayerCard(any(), any())).thenReturn(playerCard1)
    whenever(playerController2.choosePlayerCard(any(), any())).thenReturn(playerCard2)

    gameModel.startDay()

    verify(playerController1).choosePlayerCard(any(), any())
    verify(playerController2).choosePlayerCard(any(), any())
    verify(apresDeck, times(MutableGameModel.APRES_SLOTS)).drawForDay(any())
    verify(playerController1).getStartingLocation(any(), any())
    verify(playerController2).getStartingLocation(any(), any())
    Unit
  }

  @Test
  fun `turn advances sub-turn and eventually player`() = runBlocking {
    // Player 1's turn
    gameModel.currentPlayer = player1
    player1.location = createHexPoint(0, 0)
    whenever(playerController1.makeMountainDecision(any(), any()))
      .thenReturn(mountainDecision {
        rest = com.google.protobuf.Empty.getDefaultInstance()
      })

    assertThat(gameModel.currentPlayer).isEqualTo(player1)
    gameModel.turn()
    assertThat(gameModel.currentPlayer).isEqualTo(player2)
    assertThat(gameModel.clock.subTurn).isEqualTo(1)
  }

  @Test
  fun `executeRest refreshes decks`() = runBlocking {
    gameModel.currentPlayer = player1
    gameModel.currentPlayer.location = createHexPoint(0, 0)
    whenever(
      playerController1.makeMountainDecision(
        any(),
        any()
      )
    ).thenReturn(mountainDecision { rest = com.google.protobuf.Empty.getDefaultInstance() })
    gameModel.turn()
    verify(player1).refreshDecksAndChips()
  }
}
