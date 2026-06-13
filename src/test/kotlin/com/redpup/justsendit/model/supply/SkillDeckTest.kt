package com.redpup.justsendit.model.supply

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.model.supply.proto.skillCard
import com.redpup.justsendit.model.supply.testing.FakeSkillDeck
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SkillDeckTest {

  @Inject @StarterDeck private lateinit var starterDeck: SkillDeck

  @BeforeEach
  fun setup() {
    Guice.createInjector(
      com.redpup.justsendit.model.supply.testing.FakeSupplyModule(),
      com.redpup.justsendit.model.apres.testing.FakeApresModule(),
      com.redpup.justsendit.model.player.testing.FakePlayerModule()
    ).injectMembers(this)
    starterDeck.reset()
    (starterDeck as FakeSkillDeck).add(
      *(1..10).map { skillCard { name = if (it == 1) "Green Starter" else "Starter $it" } }.toTypedArray()
    )
  }

  @Test
  fun `draw returns a card`() {
    val card = starterDeck.draw()
    assertThat(card).isNotNull()
    assertThat(card.name).isNotEmpty()
  }

  @Test
  fun `deck is finite`() {
    // Starter deck has 10 cards
    repeat(10) { starterDeck.draw() }

    assertThrows(NoSuchElementException::class.java) {
      starterDeck.draw()
    }
  }

  @Test
  fun `find returns correct card and removes it`() {
    val card = starterDeck.find("Green Starter")
    assertThat(card.name).isEqualTo("Green Starter")
    
    // There should be 9 cards left
    repeat(9) { starterDeck.draw() }
    
    assertThrows(NoSuchElementException::class.java) {
      starterDeck.draw()
    }
  }
}
