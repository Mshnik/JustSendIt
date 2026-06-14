package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.player.proto.playerCard
import com.redpup.justsendit.model.player.testing.FakePlayerFactory
import com.redpup.justsendit.model.player.cards.testing.FakePlayerCard
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.random.testing.FakeRandom
import java.io.File
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PlayerDeckTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var playerFile: File
  private lateinit var factory: FakePlayerFactory
  private val random = FakeRandom()

  @BeforeEach
  fun setup() {
    playerFile = File(tempDir, "players.textproto")
    playerFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/player/player.proto
            # proto-message: PlayerCardList

            player {
              name: "Friday Card"
              day: DAY_FRIDAY
            }
            player {
              name: "Saturday Card"
              day: DAY_SATURDAY
            }
        """.trimIndent()
    )

    factory = FakePlayerFactory()
    factory.register("Friday Card") { FakePlayerCard(it) }
    factory.register("Saturday Card") { FakePlayerCard(it) }
  }

  @Test
  fun `reads cards from file`() {
    val deck = PlayerDeckImpl(playerFile.absolutePath, random, factory)
    deck.reset()

    val fridayCards = deck.getCards()[Day.DAY_FRIDAY]
    assertThat(fridayCards).hasSize(1)
    assertThat(fridayCards!![0].name).isEqualTo("Friday Card")

    val saturdayCards = deck.getCards()[Day.DAY_SATURDAY]
    assertThat(saturdayCards).hasSize(1)
    assertThat(saturdayCards!![0].name).isEqualTo("Saturday Card")
  }

  @Test
  fun `draw returns a card for specific day`() {
    val deck = PlayerDeckImpl(playerFile.absolutePath, random, factory)
    deck.reset()

    val card = deck.draw(Day.DAY_FRIDAY)
    assertThat(card.name).isEqualTo("Friday Card")
    assertThat(deck.getCards()[Day.DAY_FRIDAY]).isEmpty()
  }

  @Test
  fun `draw count returns list of cards`() {
    playerFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/player/player.proto
            # proto-message: PlayerCardList

            player { name: "Card 1"; day: DAY_FRIDAY }
            player { name: "Card 2"; day: DAY_FRIDAY }
        """.trimIndent()
    )
    factory.register("Card 1") { FakePlayerCard(it) }
    factory.register("Card 2") { FakePlayerCard(it) }
    
    val deck = PlayerDeckImpl(playerFile.absolutePath, random, factory)
    deck.reset()

    val cards = deck.draw(Day.DAY_FRIDAY, 2)
    assertThat(cards).hasSize(2)
    assertThat(cards.map { it.name }).containsExactly("Card 1", "Card 2")
  }
}
