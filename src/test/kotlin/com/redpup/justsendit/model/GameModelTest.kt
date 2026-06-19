package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.control.testing.FakePlayerControllerModule
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.apres.testing.FakeApres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import com.redpup.justsendit.model.apres.testing.FakeApresModule
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.clock.ClockModule
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.testing.FakePlayerCard
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.testing.FakePlayerFactory
import com.redpup.justsendit.model.player.testing.FakePlayerModule
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.random.testing.FakeRandomModule
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.model.skill.testing.FakeSkillModule
import com.redpup.justsendit.model.supply.ShopDeck
import com.redpup.justsendit.model.supply.SkillDeck
import com.redpup.justsendit.model.supply.StarterDeck
import com.redpup.justsendit.model.supply.proto.playerCard
import com.redpup.justsendit.model.supply.proto.skillCard
import com.redpup.justsendit.model.supply.testing.FakeApresDeck
import com.redpup.justsendit.model.supply.testing.FakePlayerDeck
import com.redpup.justsendit.model.supply.testing.FakeSkillDeck
import com.redpup.justsendit.model.supply.testing.FakeSupplyModule
import com.redpup.justsendit.util.testing.FakeTimeSourceModule
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class GameModelTest {
  private val playerController1: PlayerController = mock()
  private val playerController2: PlayerController = mock()

  @Inject private lateinit var gameModel: MutableGameModel
  @Inject private lateinit var playerDeck: FakePlayerDeck
  @Inject private lateinit var playerFactory: FakePlayerFactory
  @Inject @StarterDeck private lateinit var starterDeck: SkillDeck
  @Inject @ShopDeck private lateinit var shopDeck: SkillDeck
  @Inject private lateinit var apresDeck: FakeApresDeck
  @Inject private lateinit var apresFactory: FakeApresFactory
  @Inject private lateinit var skillFactory: SkillFactory
  @Inject private lateinit var random: Random

  private lateinit var player1: MutablePlayer
  private lateinit var player2: MutablePlayer

  @BeforeEach
  fun setup() {
    whenever(playerController1.name).thenReturn("PlayerController1")
    whenever(playerController2.name).thenReturn("PlayerController2")

    Guice.createInjector(
      ClockModule(),
      FakeTimeSourceModule(),
      FakeApresModule(),
      FakeSupplyModule(),
      FakePlayerModule(),
      FakePlayerControllerModule(listOf(playerController1, playerController2)),
      FakeRandomModule(),
      FakeSkillModule(),
      LoggerModule()
    ).injectMembers(this)

    repeat(10) {
      (starterDeck as FakeSkillDeck).add(skillCard { name = "Starter $it"; greenDice = 1 })
    }
    repeat(10) {
      (shopDeck as FakeSkillDeck).add(skillCard { name = "Shop $it"; blueDice = 1 })
    }

    playerDeck.add(playerCard {
      name = "1"
      day = Day.DAY_FRIDAY
    }, playerCard {
      name = "2"
      day = Day.DAY_FRIDAY
    }, playerCard {
      name = "3"
      day = Day.DAY_FRIDAY
    }, playerCard {
      name = "4"
      day = Day.DAY_FRIDAY
    })

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
    whenever(playerController1.chooseMountainTile(any(), any(), any(), any())).thenReturn(
      createHexPoint(0, 0)
    )
    whenever(playerController2.chooseMountainTile(any(), any(), any(), any())).thenReturn(
      createHexPoint(0, 0)
    )
    wheneverBlocking {
      playerController1.choosePlayerCard(any(), any(), any())
    }.thenAnswer { it.getArgument<List<PlayerCard>>(2).first() }
    wheneverBlocking {
      playerController2.choosePlayerCard(any(), any(), any())
    }.thenAnswer { it.getArgument<List<PlayerCard>>(2).first() }

    gameModel.startGame()

    assertThat(gameModel.apres).hasSize(MutableGameModel.APRES_SLOTS)

    verify(playerController1).choosePlayerCard(any(), any(), any())
    verify(playerController2).choosePlayerCard(any(), any(), any())
    verify(playerController1).chooseMountainTile(any(), any(), any(), any())
    verify(playerController2).chooseMountainTile(any(), any(), any(), any())
    Unit
  }

  @Test
  fun `turn advances player`() = runBlocking {
    gameModel.clock.startGame()
    gameModel.clock.startDay()
    gameModel.clock.startRound()

    player1.location = createHexPoint(0, 0)
    whenever(
      playerController1.makeMountainDecision(
        any(),
        any()
      )
    ).thenReturn(MountainDecision.DECISION_PASS)
    whenever(
      playerController1.chooseSkillCards(
        any(),
        any(),
        any(),
        any(),
        any(),
        anyVararg()
      )
    )
      .thenReturn(listOf())

    assertThat(gameModel.currentPlayer).isEqualTo(player1)
    gameModel.turn()
    // player1 passed, so currentPlayer should move to player2
    assertThat(gameModel.currentPlayer).isEqualTo(player2)
  }

  @Test
  fun `executePass discards in play cards`() = runBlocking {
    gameModel.clock.startGame()
    gameModel.clock.startDay()
    gameModel.clock.startRound()

    player1.location = createHexPoint(0, 0)
    repeat(2) { player1.gainSkill(skillFactory.create(skillCard { name = "Card $it" })) }
    player1.drawCards(2, random)
    player1.playCard(player1.hand.first())
    player1.playCard(player1.hand.first())
    whenever(
      playerController1.makeMountainDecision(
        any(), any()
      )
    ).thenReturn(MountainDecision.DECISION_PASS)
    whenever(
      playerController1.chooseSkillCards(
        any(),
        any(),
        any(),
        any(),
        any(),
        anyVararg()
      )
    )
      .thenReturn(listOf())
    gameModel.turn()
    assertThat(player1.inPlay).isEmpty()
  }
}
