package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.apres.testing.FakeApres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import java.io.File
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ApresDeckTest {

  @TempDir
  lateinit var tempDir: File

  private lateinit var apresFile: File
  private lateinit var factory: FakeApresFactory

  @BeforeEach
  fun setup() {
    apresFile = File(tempDir, "apres.textproto")
    apresFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/apres/apres.proto
            # proto-message: ApresCardList

            apres {
              name: "Day 1 Only"
              available_days: 1
            }
            apres {
              name: "Day 2 Only"
              available_days: 2
            }
            apres {
              name: "Day 1 and 2"
              available_days: 1
              available_days: 2
            }
        """.trimIndent()
    )

    factory = FakeApresFactory()
    factory.register("Day 1 Only") { FakeApres(it) }
    factory.register("Day 2 Only") { FakeApres(it) }
    factory.register("Day 1 and 2") { FakeApres(it) }
  }


  @Test
  fun `reads cards from file`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    deck.reset()

    assertThat(deck.getCards()).containsExactly(
      apresCard {
        name = "Day 1 Only"
        availableDays += 1
      },
      apresCard {
        name = "Day 2 Only"
        availableDays += 2
      },
      apresCard {
        name = "Day 1 and 2"
        availableDays += 1
        availableDays += 2
      }
    )
  }

  @Test
  fun `draw returns a card and reduces deck size`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    deck.reset()

    val card = deck.draw()
    assertThat(card).isNotNull()
    assertThat(deck.getCards()).hasSize(2)
  }

  @Test
  fun `tuck adds card to bottom of deck`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    deck.reset()
    val card = deck.draw()
    deck.tuck(card)
    assertThat(deck.getCards()).hasSize(3)
  }

  @Test
  fun `reset restores deck to initial state`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    deck.draw()
    deck.reset()
    assertThat(deck.getCards()).hasSize(3)
  }

  @Test
  fun `drawForDay gets a valid card`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    deck.reset()

    val day1Card = deck.drawForDay(1)
    assertThat(day1Card.apresCard.name).contains("1")

    val day2Card = deck.drawForDay(2)
    assertThat(day2Card.apresCard.name).contains("2")

    assertThat(deck.getCards()).hasSize(1)
  }

  @Test
  fun `drawForDay throws exception if no card is found`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, factory)
    assertThrows(IllegalArgumentException::class.java) {
      deck.drawForDay(3)
    }
  }
}
