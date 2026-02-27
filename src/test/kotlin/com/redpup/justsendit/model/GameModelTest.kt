package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Provider
import com.google.protobuf.empty
import com.redpup.justsendit.control.player.PlayerController
import com.redpup.justsendit.control.player.testing.FakePlayerControllerModule
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
import org.mockito.kotlin.verify

class GameModelTest {
  private val testDecisionHandler = TestDecisionController()

  private val apresApply = mock<(ApresCard, MutablePlayer, Boolean, GameModel) -> Unit>()

  private val abilityHandler = TestAbilityHandler()

  @Inject private lateinit var skillDecks: FakeSkillDecks
  @Inject private lateinit var apresDeck: FakeApresDeck
  @Inject private lateinit var playerDeck: FakePlayerDeck
  @Inject private lateinit var apresFactory: FakeApresFactory
  @Inject private lateinit var playerFactory: FakePlayerFactory
  @Inject private lateinit var tileSupply: FakeTileSupply
  @Inject private lateinit var fakeTimeSource: FakeTimeSource
  @Inject private lateinit var testLogger: TestLogger
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
      },
      playerCard {
        name = "Andy"
      }
    ))

    apresDeck.apresCards.addAll(
      listOf(
        apresCard {
          name = "Day 1 Only A"
          availableDays += 1
        },
        apresCard {
          name = "Day 1 Only B"
          availableDays += 1
        },
        apresCard {
          name = "Day 1 Only C"
          availableDays += 1
        },
        apresCard {
          name = "Day 2 Only A"
          availableDays += 2
        },
        apresCard {
          name = "Day 2 Only B"
          availableDays += 2
        },
        apresCard {
          name = "Day 2 Only C"
          availableDays += 2
        },
        apresCard {
          name = "Day 3 Only A"
          availableDays += 3
        },
        apresCard {
          name = "Day 3 Only B"
          availableDays += 3
        },
        apresCard {
          name = "Day 3 Only C"
          availableDays += 3
        },
      )
    )

    for (card in apresDeck.apresCards) {
      apresFactory.register(card.name) { FakeApres(it).onApply(apresApply) }
    }

    for (card in playerDeck.cards) {
      playerFactory.register(card.name, abilityHandler)
    }

    game = gameProvider.get()
  }

  @Test
  fun `game model initializes correctly`() = runBlocking {
    skillDecks.setGreenDeck(List(10) { 1 }) // Set up for starting deck
    assertThat(game.players.size).isEqualTo(2)
    assertThat(game.tileMap.size()).isEqualTo(3)
    assertThat(game.players[0].skillDeck.size).isEqualTo(10) // Starting deck
    assertThat(game.players[0].location).isEqualTo(createHexPoint(0, 0))
    assertThat(game.apres).hasSize(3)
    assertThat(game.apres.all { 1 in it.apresCard.availableDaysList }).isTrue()
  }

  @Test
  fun `turn with REST decision works`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      skillDeck.clear() // Clear existing cards from buyStartingDeck
      skillDeck.addAll(listOf(1, 2, 3))
      playSkillCard() // card -> discard
    }
    assertThat(player.skillDiscard.size).isEqualTo(1)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = empty {}
    })

    game.turn()

    assertThat(player.skillDiscard.size).isEqualTo(0)
    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(game.clock.turn).isEqualTo(1)
  }

  @Test
  fun `turn with LIFT decision works`() = runBlocking {
    val player = game.players[0]
    player.mutate { location = createHexPoint(0, 0) }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      lift = empty {}
    })

    game.turn()

    assertThat(player.location).isEqualTo(hexPoint { q = 0; r = -1 })
    assertThat(player.apresLink).isNull()
    assertThat(game.clock.turn).isEqualTo(1)
  }

  @Test
  fun `turn with EXIT decision works`() = runBlocking {
    val player = game.players[0]
    player.mutate { location = createHexPoint(0, 0) } // Location with apres_link

    testDecisionHandler.decisionQueue.add(mountainDecision {
      exit = empty {}
    })

    game.turn()

    assertThat(player.location).isNull()
    assertThat(player.apresLink).isEqualTo(1)
    assertThat(player.isOnMountain).isFalse()
    assertThat(game.clock.turn).isEqualTo(1)

    verify(apresApply).invoke(eq(game.apres[0].apresCard), any(), eq(true), any())
  }

  @Test
  fun `successful SKI_RIDE turn with success`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(6)) // Guaranteed success (6 > 5)
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })
    testDecisionHandler.decisionQueue.add(mountainDecision {
      pass = empty {}
    }) // End turn

    game.turn()

    assertThat(player.day.experience).isEqualTo(0)
    assertThat(player.day.mountainPoints).isEqualTo(5)
    assertThat(player.apresLink).isNull()
  }

  @Test
  fun `successful SKI_RIDE turn with exact success`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(5)) // Guaranteed exact success (5 == 5)
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })
    testDecisionHandler.decisionQueue.add(mountainDecision {
      pass = empty {}
    }) // End turn

    game.turn()

    assertThat(player.day.experience).isEqualTo(1)
    assertThat(player.day.mountainPoints).isEqualTo(5)
    assertThat(player.apresLink).isNull()
  }

  @Test
  fun `successful SKI_RIDE turn with partial failure`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(3)) // Guaranteed partial success (2.5 <= 3 < 5)
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    }) // Ends turn

    game.turn()

    assertThat(player.day.experience).isEqualTo(1)
    assertThat(player.day.mountainPoints).isEqualTo(0)
    assertThat(player.apresLink).isNull()
  }

  @Test
  fun `successful SKI_RIDE turn with full failure`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(2)) // Guaranteed full success (2 <= 2.5)
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    }) // Ends turn

    game.turn()

    assertThat(player.day.experience).isEqualTo(0)
    assertThat(player.day.mountainPoints).isEqualTo(0)
    assertThat(player.apresLink).isNull()
  }

  @Test
  fun `successful SKI_RIDE turn with success with overkill`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(10)) // Guaranteed success (10 > 5)
      day.overkillBonusPoints = OverkillBonus(5, 4)
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })
    testDecisionHandler.decisionQueue.add(mountainDecision {
      pass = empty {}
    }) // End turn

    game.turn()

    assertThat(player.day.experience).isEqualTo(0)
    assertThat(player.day.mountainPoints).isEqualTo(9) // 5 + 4
    assertThat(player.apresLink).isNull()
  }

  @Test
  fun `advance day updates day and repopulates apres`() = runBlocking {
    testDecisionHandler.startLocation = hexPoint { q = -1; r = -1 }
    game.advanceDay()

    assertThat(game.clock.day).isEqualTo(2)
    assertThat(game.clock.turn).isEqualTo(1)
    assertThat(game.players[0].location).isEqualTo(hexPoint { q = -1; r = -1 })
    assertThat(game.apres).hasSize(3)
    assertThat(game.apres.all { 2 in it.apresCard.availableDaysList }).isTrue()
  }

  @Test
  fun `turn with REST decision adds log with basic params`() = runBlocking {
    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = empty {}
    })

    game.turn()

    assertThat(testLogger.logs).hasSize(1)
    val log = testLogger.logs[0]
    assertThat(log.timestamp.seconds).isGreaterThan(0)
    assertThat(log.day).isEqualTo(1)
    assertThat(log.turn).isEqualTo(1)
    assertThat(log.subturn).isEqualTo(1)
    assertThat(log.playerName).isEqualTo("Amy")
    assertThat(log.controllerName).isEqualTo("TestDecisionController")
  }

  @Test
  fun `turn with REST decision adds MountainDecision log`() = runBlocking {
    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = empty {}
    })

    game.turn()

    assertThat(testLogger.logs).hasSize(1)
    val log = testLogger.logs[0]
    assertThat(log.hasMountainDecision()).isTrue()
    assertThat(log.mountainDecision.hasRest()).isTrue()
  }

  @Test
  fun `turn with LIFT decision adds PlayerMove log`() = runBlocking {
    val player = game.players[0]
    player.mutate { location = createHexPoint(0, 0) }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      lift = empty {}
    })

    game.turn()

    assertThat(testLogger.logs).hasSize(2) // 1 for choice, 1 for move
    val log = testLogger.logs[1]
    assertThat(log.hasPlayerMove()).isTrue()
    assertThat(log.playerMove.from).isEqualTo(createHexPoint(0, 0))
    assertThat(log.playerMove.to).isEqualTo(hexPoint { q = 0; r = -1 })
  }

  @Test
  fun `successful SKI_RIDE turn with success adds SkillCardDraw logs`() = runBlocking {
    val player = game.players[0]
    player.mutate {
      location = createHexPoint(0, 0)
      skillDeck.clear()
      skillDeck.addAll(listOf(6))
    }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })
    testDecisionHandler.decisionQueue.add(mountainDecision {
      pass = empty {}
    }) // End turn

    game.turn()

    assertThat(testLogger.logs).hasSize(4) // choice, ski/ride move, card draw, choice
    val log = testLogger.logs[2]
    assertThat(log.hasSkillCardDraw()).isTrue()
    assertThat(log.skillCardDraw.cardValueList).containsExactly(6)
    Unit
  }


  @Test
  fun `turn advances players and clock correctly`() = runBlocking {
    assertThat(game.players).hasSize(2)
    assertThat(game.currentPlayer.playerCard.name).isEqualTo("Amy")
    assertThat(game.clock.turn).isEqualTo(1)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })

    // Player 1's turn
    game.turn()
    assertThat(game.currentPlayer.playerCard.name).isEqualTo("Andy")
    assertThat(game.clock.turn).isEqualTo(1)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 1
      }
    })

    // Player 2's turn, wraps around
    game.turn()
    assertThat(game.currentPlayer.playerCard.name).isEqualTo("Amy")
    assertThat(game.clock.turn).isEqualTo(2)
  }

  /** A test implementation of [PlayerController] that returns decisions from a queue. */
  class TestDecisionController : PlayerController {
    override val name = "TestDecisionController"
    val decisionQueue = mutableListOf<MountainDecision>()
    var startLocation = createHexPoint(0, 0)

    override suspend fun makeMountainDecision(
      player: Player,
      gameModel: GameModel,
    ): MountainDecision {
      return decisionQueue.removeFirstOrNull() ?: mountainDecision {
        pass = empty {}
      }
    }

    override suspend fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
      return startLocation
    }

    override suspend fun chooseCardsToRemove(
      player: Player,
      cards: List<Int>,
      maxToRemove: Int,
    ): List<Int> {
      TODO("Not yet implemented")
    }

    override suspend fun shouldGainSpeed(player: Player): Boolean {
      TODO("Not yet implemented")
    }

    override suspend fun chooseMoveOnRest(player: Player): HexDirection? {
      return null
    }

    override suspend fun decideToUseEndurance(): Boolean {
      return false
    }

    override suspend fun onRevealTopCard(card: Int) {}
  }

  class TestAbilityHandler : AbilityHandler {}
}
