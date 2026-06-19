package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.apres.proto.apresCard
import com.redpup.justsendit.model.apres.testing.FakeApres
import com.redpup.justsendit.model.apres.testing.FakeApresFactory
import com.redpup.justsendit.model.proto.Day
import com.redpup.justsendit.model.random.testing.FakeRandom
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
  private val random = FakeRandom()

  @BeforeEach
  fun setup() {
    apresFile = File(tempDir, "apres.textproto")
    apresFile.writeText(
      """
            # proto-file: com/redpup/justsendit/model/apres/apres.proto
            # proto-message: ApresCardList

            apres {
              name: "Friday Only"
              available_days: 2
            }
            apres {
              name: "Saturday Only"
              available_days: 3
            }
            apres {
              name: "Friday and Saturday"
              available_days: 2
              available_days: 3
            }
        """.trimIndent()
    )

    factory = FakeApresFactory()
    factory.register("Friday Only") { FakeApres(it) }
    factory.register("Saturday Only") { FakeApres(it) }
    factory.register("Friday and Saturday") { FakeApres(it) }
  }


  @Test
  fun `reads cards from file`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    deck.reset()

    assertThat(deck.getCards()).containsExactly(
      apresCard {
        name = "Friday Only"
        availableDays += Day.DAY_FRIDAY
      },
      apresCard {
        name = "Saturday Only"
        availableDays += Day.DAY_SATURDAY
      },
      apresCard {
        name = "Friday and Saturday"
        availableDays += Day.DAY_FRIDAY
        availableDays += Day.DAY_SATURDAY
      }
    )
  }

  @Test
  fun `draw returns a card and reduces deck size`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    deck.reset()

    val card = deck.draw()
    assertThat(card).isNotNull()
    assertThat(deck.getCards()).hasSize(2)
  }

  @Test
  fun `tuck adds card to bottom of deck`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    deck.reset()
    val card = deck.draw()
    deck.tuck(card)
    assertThat(deck.getCards()).hasSize(3)
  }

  @Test
  fun `reset restores deck to initial state`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    deck.draw()
    deck.reset()
    assertThat(deck.getCards()).hasSize(3)
  }

  @Test
  fun `drawForDay gets a valid card`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    deck.reset()

    val day1Card = deck.drawForDay(Day.DAY_FRIDAY)
    assertThat(day1Card.apresCard.name).contains("Friday")

    val day2Card = deck.drawForDay(Day.DAY_SATURDAY)
    assertThat(day2Card.apresCard.name).contains("Saturday")

    assertThat(deck.getCards()).hasSize(1)
  }

  @Test
  fun `drawForDay throws exception if no card is found`() {
    val deck = ApresDeckImpl(apresFile.absolutePath, random, factory)
    assertThrows(IllegalArgumentException::class.java) {
      deck.drawForDay(Day.DAY_SUNDAY)
    }
  }
}
