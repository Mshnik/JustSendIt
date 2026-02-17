package com.redpup.justsendit.model

import com.google.protobuf.Empty
import com.google.protobuf.empty
import com.redpup.justsendit.model.board.grid.HexExtensions.HexPoint
import com.redpup.justsendit.model.board.hex.proto.HexDirection
import com.redpup.justsendit.model.board.hex.proto.HexPoint
import com.redpup.justsendit.model.board.hex.proto.hexPoint
import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.model.player.PlayerHandler
import com.redpup.justsendit.model.player.proto.MountainDecision
import com.redpup.justsendit.model.player.proto.MountainDecisionKt.skiRideDecision
import com.redpup.justsendit.model.player.proto.mountainDecision
import com.redpup.justsendit.model.supply.SkillDecksInstance
import java.io.File
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GameModelTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var tilesFile: File
  private lateinit var locationsFile: File
  private lateinit var playersFile: File

  private val testDecisionHandler = TestDecisionHandler()

  @BeforeEach
  fun setup() {
    tilesFile = File(tempDir, "tiles.textproto")
    locationsFile = File(tempDir, "locations.textproto")
    playersFile = File(tempDir, "players.textproto")

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
  }

  private fun createGameModel(playerCount: Int = 1) = MutableGameModel(
    tilesPath = tilesFile.absolutePath,
    locationsPath = locationsFile.absolutePath,
    playersPath = playersFile.absolutePath,
    playerHandlers = List(playerCount) { testDecisionHandler },
    skillDecks = SkillDecksInstance
  )

  @Test
  fun `game model initializes correctly`() {
    val game = createGameModel(2)
    assertEquals(2, game.players.size)
    assertEquals(3, game.tileMap.size())
    assertEquals(10, game.players[0].skillDeck.size) // Starting deck
    assertEquals(HexPoint(0, 0), game.players[0].location)
  }

  @Test
  fun `turn with REST decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.skillDeck.addAll(listOf(1, 2, 3))
    player.playSkillCard() // card -> discard
    assertEquals(1, player.skillDiscard.size)

    testDecisionHandler.decisionQueue.add(mountainDecision {
      rest = Empty.getDefaultInstance()
    })

    game.turn()

    assertEquals(0, player.skillDiscard.size)
    assertEquals(3, player.skillDeck.size)
    assertEquals(2, game.clock.turn)
  }

  @Test
  fun `turn with LIFT decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.location = hexPoint { q = 0; r = 0 }

    testDecisionHandler.decisionQueue.add(mountainDecision {
      lift = Empty.getDefaultInstance()
    })

    game.turn()

    assertEquals(hexPoint { q = 0; r = -1 }, player.location)
    assertEquals(2, game.clock.turn)
  }

  @Test
  fun `turn with EXIT decision works`() {
    val game = createGameModel()
    val player = game.players[0]
    player.location = hexPoint { q = 0; r = 0 } // Location with apres_link

    testDecisionHandler.decisionQueue.add(mountainDecision {
      exit = Empty.getDefaultInstance()
    })

    game.turn()

    assertNull(player.location)
    assertFalse(player.isOnMountain)
    assertEquals(2, game.clock.turn)
  }

  @Test
  fun `successful SKI_RIDE turn`() {
    val game = createGameModel()
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
      pass = Empty.getDefaultInstance()
    }) // End turn

    game.turn()

    assertEquals(5, player.points)
    // turn.speed is reset after turn, so we can't check it here.
    assertEquals(0, player.experience)
  }

  @Test
  fun `cleanup advances day`() {
    val game = createGameModel()
    testDecisionHandler.startLocation = hexPoint { q = -1; r = -1 }
    game.cleanup()

    assertEquals(2, game.clock.day)
    assertEquals(1, game.clock.turn)
    assertEquals(hexPoint { q = -1;r = -1 }, game.players[0].location)
  }

  /** A test implementation of [PlayerHandler] that returns decisions from a queue. */
  class TestDecisionHandler : PlayerHandler {
    val decisionQueue = mutableListOf<MountainDecision>()
    var startLocation = HexPoint(0, 0)

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
