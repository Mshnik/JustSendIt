package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Provider
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.control.player.testing.FakePlayerControllerModule
import com.redpup.justsendit.log.proto.skiRideAttempt
import com.redpup.justsendit.log.testing.TestLogger
import com.redpup.justsendit.log.testing.TestLoggerModule
import com.redpup.justsendit.model.apres.proto.ApresCard
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.apres.testing.FakeApres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import com.redpup.justsendit.model.apres.testing.FakeApresModule
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.board.tile.proto.*
import com.redpup.justsendit.model.player.AbilityHandler
import com.redpup.justsendit.model.player.MutablePlayer
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.Player.Day.OverkillBonus
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.testing.FakePlayerFactory
import com.redpup.justsendit.model.player.testing.FakePlayerModule
import com.redpup.justsendit.model.proto.Grade
import com.redpup.justsendit.model.supply.proto.upgradeCard
import com.redpup.justsendit.model.supply.testing.*
import com.redpup.justsendit.util.testing.FakeTimeSource
import com.redpup.justsendit.util.testing.FakeTimeSourceModule
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GameModelTest {
  private val testDecisionHandler = TestDecisionController()

  private val apresApply = mock<(ApresCard, MutablePlayer, Boolean, GameModel) -> Unit>()

  @Inject private lateinit var skillDecks: FakeSkillDecks
  @Inject private lateinit var apresDeck: FakeApresDeck
  @Inject private lateinit var playerDeck: FakePlayerDeck
  @Inject private lateinit var apresFactory: FakeApresFactory
  @Inject private lateinit var playerFactory: FakePlayerFactory
  @Inject private lateinit var tileSupply: FakeTileSupply
  @Inject private lateinit var fakeTimeSource: FakeTimeSource
  @Inject private lateinit var testLogger: TestLogger
  @Inject private lateinit var upgradeDeck: FakeUpgradeDeck
  @Inject private lateinit var gameProvider: Provider<MutableGameModel>

  private lateinit var game: MutableGameModel

  @BeforeEach
  fun setup() {
    Guice.createInjector(
      FakePlayerModule(),
      FakePlayerControllerModule(List(2) { _ -> testDecisionHandler }),
      FakeApresModule(),
      FakeSupplyModule(),
      FakeTimeSourceModule(),
      TestLoggerModule(),
    ).injectMembers(this)

    fakeTimeSource.now = Instant.ofEpochMilli(12345)
    fakeTimeSource.autoAdvance = Duration.ofSeconds(1)

    skillDecks.setGreenDeck(List(100) { 1 })
    skillDecks.setBlueDeck(List(100) { 4 })
    skillDecks.setBlackDeck(List(100) { 7 })

    tileSupply.tiles = listOf(
      mountainTile {
        slope = slopeTile {
          difficulty = 5
          grade = Grade.GRADE_GREEN
        }
      },
      mountainTile {
        lift = liftTile {
          color = LiftColor.LIFT_COLOR_RED
          direction = LiftDirection.LIFT_DIRECTION_BOTTOM
        }
        apresLink = 1
      },
      mountainTile {
        lift = liftTile {
          color = LiftColor.LIFT_COLOR_RED
          direction = LiftDirection.LIFT_DIRECTION_TOP
        }
      }
    )

    tileSupply.locations = listOf(
      mountainTileLocation {
        point = createHexPoint(0, 1)
        grade = Grade.GRADE_GREEN
      },
      mountainTileLocation {
        point = createHexPoint(0, 0)
        lift = liftTile {
          color = LiftColor.LIFT_COLOR_RED
          direction = LiftDirection.LIFT_DIRECTION_BOTTOM
        }
      },
      mountainTileLocation {
        point = createHexPoint(0, -1)
        lift = liftTile {
          color = LiftColor.LIFT_COLOR_RED
          direction = LiftDirection.LIFT_DIRECTION_TOP
        }
      }
    )

    playerDeck.cards.addAll(listOf(
      playerCard {
        name = "Amy"
        startingSkillCards.add(Grade.GRADE_GREEN)
      },
      playerCard {
        name = "Andy"
        startingSkillCards.add(Grade.GRADE_BLUE)
      }
    ))

    apresDeck.apresCards.addAll(
      listOf(
        apresCard {
          name = "Day 1 Only A"
          availableDays += 1
        },
      )
    )

    for (card in apresDeck.apresCards) {
      apresFactory.register(card.name) { FakeApres(it).onApply(apresApply) }
    }

    for (card in playerDeck.cards) {
      playerFactory.register(card.name) { p -> object: AbilityHandler {} }
    }

    game = gameProvider.get()
  }

  @Test
  fun `game model initializes correctly`() = runBlocking {
    assertThat(game.players.size).isEqualTo(2)
    assertThat(game.tileMap.size()).isEqualTo(3)
    assertThat(game.players[0].skillDeck.size).isEqualTo(1)
    assertThat(game.players[0].location).isEqualTo(createHexPoint(0, 0))
    assertThat(game.apres).hasSize(3)
  }

  @Test
  fun `turn with REST refreshes chips`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      terrainChips.add(com.redpup.justsendit.model.player.proto.terrainChip { value = 1 })
      usedTerrainChips.add(com.redpup.justsendit.model.player.proto.terrainChip { value = 2 })
    }
    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = empty {}
    })
    game.turn()
    assertThat(player.terrainChips).hasSize(2)
    assertThat(player.usedTerrainChips).isEmpty()
  }
  
  @Test
  fun `crash with chip usage`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(1)) // Guaranteed failure
      terrainChips.add(com.redpup.justsendit.model.player.proto.terrainChip { value = 5 })
    }
    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })
    testDecisionHandler.chipsToUse = listOf(player.terrainChips[0])
    
    game.turn()
    
    assertThat(player.turn.points).isGreaterThan(0)
  }
  
  @Test
  fun `advanceDay triggers upgrade phase`() = runBlocking {
      upgradeDeck.cards.add(upgradeCard { name = "test" })
      game.advanceDay()
      assertThat(upgradeDeck.cards).isEmpty()
  }

  @Test
  fun `turn with LIFT decision refreshes chips`() = runBlocking {
    val player = game.players[0]
    player.mutate { 
        location = createHexPoint(0, 0)
        terrainChips.add(com.redpup.justsendit.model.player.proto.terrainChip { value = 1 })
        usedTerrainChips.add(com.redpup.justsendit.model.player.proto.terrainChip { value = 2 })
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      lift = empty {}
    })

    game.turn()

    assertThat(player.location).isEqualTo(hexPoint { q = 0; r = -1 })
    assertThat(player.terrainChips).hasSize(2)
    assertThat(player.usedTerrainChips).isEmpty()
  }

  // ... other tests ...
}
