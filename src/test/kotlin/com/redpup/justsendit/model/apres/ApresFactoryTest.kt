package com.redpup.justsendit.model.apres

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.apresCard
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApresFactoryTest {

  private val factory = ApresFactoryImpl()

  @Test
  fun `create Tune-Up`() {
    val card = apresCard { name = "Tune-Up" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(TuneUp::class.java)
  }

  @Test
  fun `create Study`() {
    val card = apresCard { name = "Study" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Study::class.java)
  }

  @Test
  fun `create First Chair`() {
    val card = apresCard { name = "First Chair" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(FirstChair::class.java)
  }

  @Test
  fun `create Bar`() {
    val card = apresCard { name = "Bar" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Bar::class.java)
  }

  @Test
  fun `create Dining`() {
    val card = apresCard { name = "Dining" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Dining::class.java)
  }

  @Test
  fun `create Village`() {
    val card = apresCard { name = "Village" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Village::class.java)
  }

  @Test
  fun `create Massage`() {
    val card = apresCard { name = "Massage" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Massage::class.java)
  }

  @Test
  fun `create Journal`() {
    val card = apresCard { name = "Journal" }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(Journal::class.java)
  }

  @Test
  fun `create unknown card throws`() {
    val card = apresCard { name = "Unknown Card" }
    assertThrows<IllegalArgumentException> {
      factory.create(card)
    }
  }
}
