package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.empty
import com.redpup.justsendit.model.board.grid.HexExtensions.createHexPoint
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.model.supply.testing.FakeSkillDecks
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GameModelTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var tilesFile: File
  private lateinit var locationsFile: File
  private lateinit var playersFile: File
  private lateinit var apresFile: File

  private val testDecisionHandler = TestDecisionHandler()

  private lateinit var skillDecks: FakeSkillDecks

  @BeforeEach
  fun setup() {
    tilesFile = File(tempDir, "tiles.textproto")
    locationsFile = File(tempDir, "locations.textproto")
    playersFile = File(tempDir, "players.textproto")
    apresFile = File(tempDir, "apres.textproto")
    skillDecks = FakeSkillDecks()
    skillDecks.setGreenDeck(List(100) { 1 })
    skillDecks.setBlueDeck(List(100) { 4 })
    skillDecks.setBlackDeck(List(100) { 7 })

    tilesFile.writeText(
      """
            tiles {
              slope {
                difficulty: 5
                grade: GRADE_GREEN
              }
            }
            tiles {
              lift {
                color: LIFT_COLOR_RED
                direction: LIFT_DIRECTION_BOTTOM
              }
              apres_link: 1
            }
            tiles {
              lift {
                color: LIFT_COLOR_RED
                direction: LIFT_DIRECTION_TOP
              }
            }
        """.trimIndent()
    )

    locationsFile.writeText(
      """
            location {
              point { q: 0 r: 1 }
              grade: GRADE_GREEN
            }
            location {
              point { q: 0 r: 0 }
              lift { color: LIFT_COLOR_RED direction: LIFT_DIRECTION_BOTTOM }
            }
            location {
              point { q: 0 r: -1 }
              lift { color: LIFT_COLOR_RED direction: LIFT_DIRECTION_TOP }
            }
        """.trimIndent()
    )

    playersFile.writeText(
      """
            player { name: "Player 1" }
            player { name: "Player 2" }
        """.trimIndent()
    )
    apresFile.writeText(
      """
        apres {
          name: "Day 1 Only A"
          available_days: 1
        }
        apres {
          name: "Day 1 Only B"
          available_days: 1
        }
        apres {
          name: "Day 1 Only C"
          available_days: 1
        }
        apres {
          name: "Day 2 Only A"
          available_days: 2
        }
        apres {
          name: "Day 2 Only B"
          available_days: 2
        }
        apres {
          name: "Day 2 Only C"
          available_days: 2
        }
         apres {
          name: "Day 3 Only A"
          available_days: 3
        }
         apres {
          name: "Day 3 Only B"
          available_days: 3
        }
         apres {
          name: "Day 3 Only C"
          available_days: 3
        }
    """.trimIndent()
    )
  }

  private fun createGameModel(
    playerCount: Int = 1,
    skillDecks: SkillDecks = this.skillDecks,
  ) =
    MutableGameModel(
      tilesPath = tilesFile.absolutePath,
      locationsPath = locationsFile.absolutePath,
      playersPath = playersFile.absolutePath,
      apresPath = apresFile.absolutePath,
      playerHandlers = List(playerCount) { testDecisionHandler },
      skillDecks = skillDecks
    )

  @Test
  fun `game model initializes correctly`() {
    skillDecks.setGreenDeck(List(10) { 1 }) // Set up for starting deck
    val game = createGameModel(2)
    assertThat(game.players.size).isEqualTo(2)
    assertThat(game.tileMap.size()).isEqualTo(3)
    assertThat(game.players[0].skillDeck.size).isEqualTo(10) // Starting deck
    assertThat(game.players[0].location).isEqualTo(createHexPoint(0, 0))
    assertThat(game.apres).hasSize(3)
    assertThat(game.apres.all { 1 in it.apresCard.availableDaysList }).isTrue()
  }

  @Test
  fun `turn with REST decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.skillDeck.clear() // Clear existing cards from buyStartingDeck
    player.skillDeck.addAll(listOf(1, 2, 3))
    player.playSkillCard() // card -> discard
    assertThat(player.skillDiscard.size).isEqualTo(1)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = empty {}
    })

    game.turn()

    assertThat(player.skillDiscard.size).isEqualTo(0)
    assertThat(player.skillDeck.size).isEqualTo(3)
    assertThat(game.clock.turn).isEqualTo(2)
  }

  @Test
  fun `turn with LIFT decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.location = hexPoint { q = 0; r = 0 }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      lift = empty {}
    })

    game.turn()

    assertThat(player.location).isEqualTo(hexPoint { q = 0; r = -1 })
    assertThat(game.clock.turn).isEqualTo(2)
  }

  @Test
  fun `turn with EXIT decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.location = hexPoint { q = 0; r = 0 } // Location with apres_link

    testDecisionHandler.decisionQueue.add(mountainDecision {
      exit = empty {}
    })

    game.turn()

    assertThat(player.location).isNull()
    assertThat(player.isOnMountain).isFalse()
    assertThat(game.clock.turn).isEqualTo(2)
  }

  @Test
  fun `successful SKI_RIDE turn`() {
    val game = createGameModel()
    game.advanceDay()
    val player = game.players[0]
    player.location = hexPoint { q = 0; r = 0 }
    player.skillDeck.clear()
    player.skillDeck.addAll(listOf(5, 5)) // Guaranteed success (5+5 > 5)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      skiRide = skiRideDecision {
        direction = HexDirection.HEX_DIRECTION_SOUTH
        numCards = 2
      }
    })
    testDecisionHandler.decisionQueue.add(mountainDecision {
      pass = empty {}
    }) // End turn

    game.turn()

    assertThat(player.points).isEqualTo(5)
    // turn.speed is reset after turn, so we can't check it here.
    assertThat(player.experience).isEqualTo(0)
  }

  @Test
  fun `advance day updates day and repopulates apres`() {
    val game = createGameModel()
    testDecisionHandler.startLocation = hexPoint { q = -1; r = -1 }
    game.advanceDay()

    assertThat(game.clock.day).isEqualTo(2)
    assertThat(game.clock.turn).isEqualTo(1)
    assertThat(game.players[0].location).isEqualTo(hexPoint { q = -1; r = -1 })
    assertThat(game.apres).hasSize(3)
    assertThat(game.apres.all { 2 in it.apresCard.availableDaysList }).isTrue()
  }

  /** A test implementation of [PlayerHandler] that returns decisions from a queue. */
  class TestDecisionHandler : PlayerHandler {
    val decisionQueue = mutableListOf<MountainDecision>()
    var startLocation = createHexPoint(0, 0)

    override fun makeMountainDecision(player: Player, gameModel: GameModel): MountainDecision {
      return decisionQueue.removeFirstOrNull() ?: mountainDecision {
        pass = empty {}
      }
    }

    override fun getStartingLocation(player: Player, gameModel: GameModel): HexPoint {
      return startLocation
    }
  }
}
