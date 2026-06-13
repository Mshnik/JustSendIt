package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.control.player.testing.FakePlayerControllerModule
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.apres.testing.FakeApres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import com.redpup.justsendit.model.apres.testing.FakeApresModule
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.testing.FakePlayerCard
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.testing.FakePlayerFactory
import com.redpup.justsendit.model.player.testing.FakePlayerModule
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.testing.FakeApresDeck
import com.redpup.justsendit.model.supply.testing.FakePlayerDeck
import com.redpup.justsendit.model.supply.testing.FakeSkillDeck
import com.redpup.justsendit.model.supply.testing.FakeSupplyModule
import com.redpup.justsendit.util.testing.FakeTimeSourceModule
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GameModelTest {
  private val playerController1: PlayerController = mock()
  private val playerController2: PlayerController = mock()

  @Inject private lateinit var gameModel: MutableGameModel
  @Inject private lateinit var playerDeck: FakePlayerDeck
  @Inject private lateinit var playerFactory: FakePlayerFactory
  @Inject private lateinit var skillDecks: FakeSkillDeck
  @Inject private lateinit var apresDeck: FakeApresDeck
  @Inject private lateinit var apresFactory: FakeApresFactory

  private lateinit var player1: MutablePlayer
  private lateinit var player2: MutablePlayer

  @BeforeEach
  fun setup() {
    whenever(playerController1.name).thenReturn("PlayerController1")
    whenever(playerController2.name).thenReturn("PlayerController2")

    Guice.createInjector(
      FakeTimeSourceModule(),
      FakeApresModule(),
      FakeSupplyModule(),
      FakePlayerModule(),
      FakePlayerControllerModule(listOf(playerController1, playerController2)),
      LoggerModule()
    ).injectMembers(this)

    skillDecks.setGreenDeck(listOf(1, 1, 2, 2, 3, 3))
    skillDecks.setBlueDeck(listOf(4, 4, 5, 5, 6, 6))
    skillDecks.setBlackDeck(listOf(7, 7, 8, 8, 9, 9))

    playerDeck.add(
      playerCard {
        name = "1"
        day = Day.DAY_FRIDAY
      },
      playerCard {
        name = "2"
        day = Day.DAY_FRIDAY
      },
      playerCard {
        name = "3"
        day = Day.DAY_FRIDAY
      },
      playerCard {
        name = "4"
        day = Day.DAY_FRIDAY
      }
    )

    playerFactory.register("1") { FakePlayerCard(it) }
    playerFactory.register("2") { FakePlayerCard(it) }
    playerFactory.register("3") { FakePlayerCard(it) }
    playerFactory.register("4") { FakePlayerCard(it) }

    apresDeck.add(
      apresCard {
        name = "1"
        availableDays += Day.DAY_FRIDAY
      },
      apresCard {
        name = "2"
        availableDays += Day.DAY_FRIDAY
      },
      apresCard {
        name = "3"
        availableDays += Day.DAY_FRIDAY
      },
      apresCard {
        name = "4"
        availableDays += Day.DAY_FRIDAY
      },
    )

    apresFactory.register("1") { FakeApres(it) }
    apresFactory.register("2") { FakeApres(it) }
    apresFactory.register("3") { FakeApres(it) }
    apresFactory.register("4") { FakeApres(it) }

    player1 = gameModel.players[0]
    player2 = gameModel.players[1]
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
    whenever(playerController1.getStartingLocation(any(), any())).thenReturn(mock())
    whenever(playerController2.getStartingLocation(any(), any())).thenReturn(mock())
    whenever(
      playerController1.choosePlayerCard(
        any(), any()
      )
    ).thenAnswer { it.getArgument<List<PlayerCard>>(1).first() }
    whenever(
      playerController2.choosePlayerCard(
        any(), any()
      )
    ).thenAnswer { it.getArgument<List<PlayerCard>>(1).first() }

    gameModel.players.forEach {
      it.mutate {
        it.gainSkillCards(
          listOf(
            Grade.GRADE_GREEN,
            Grade.GRADE_BLUE,
            Grade.GRADE_BLACK
          ), skillDecks
        )
      }
    }

    gameModel.startDay()

    assertThat(gameModel.apres).hasSize(MutableGameModel.APRES_SLOTS)

    verify(playerController1).choosePlayerCard(any(), any())
    verify(playerController2).choosePlayerCard(any(), any())
    verify(playerController1).getStartingLocation(any(), any())
    verify(playerController2).getStartingLocation(any(), any())
    Unit
  }

  @Test
  fun `turn advances sub-turn and eventually player`() = runBlocking {
    player1.location = createHexPoint(0, 0)
    whenever(playerController1.makeMountainDecision(any(), any())).thenReturn(mountainDecision {
      rest = com.google.protobuf.Empty.getDefaultInstance()
    })

    assertThat(gameModel.currentPlayer).isEqualTo(player1)
    gameModel.turn()
    assertThat(gameModel.currentPlayer).isEqualTo(player2)
    assertThat(gameModel.clock.subTurn).isEqualTo(1)
  }

  @Test
  fun `executeRest refreshes decks`() = runBlocking {
    player1.location = createHexPoint(0, 0)
    player1.gainSkillCards(listOf(Grade.GRADE_GREEN, Grade.GRADE_GREEN), skillDecks)
    player1.playSkillCard()
    player1.playSkillCard()
    whenever(
      playerController1.makeMountainDecision(
        any(), any()
      )
    ).thenReturn(mountainDecision { rest = com.google.protobuf.Empty.getDefaultInstance() })
    gameModel.turn()
    assertThat(player1.skillDiscard).isEmpty()
  }
}
