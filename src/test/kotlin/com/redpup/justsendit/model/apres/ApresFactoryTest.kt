package com.redpup.justsendit.model.apres

import com.google.common.truth.Truth.assertThat
import com.google.inject.Guice
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.apresCard
import javax.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApresFactoryTest {

  @Inject private lateinit var apresFactory: ApresFactoryImpl

  @BeforeEach
  fun setup() {
    Guice.createInjector().injectMembers(this)
  }

  @Test
  fun `create returns BuyGear for Buy Gear card`() {
    val apresCard = apresCard { name = "Buy Gear" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(BuyGear::class.java)
  }

  @Test
  fun `create returns TuneUp for Tune-Up card`() {
    val apresCard = apresCard { name = "Tune-Up" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(TuneUp::class.java)
  }

  @Test
  fun `create returns Study for Study card`() {
    val apresCard = apresCard { name = "Study" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Study::class.java)
  }

  @Test
  fun `create returns FirstChair for First Chair card`() {
    val apresCard = apresCard { name = "First Chair" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(FirstChair::class.java)
  }

  @Test
  fun `create returns Sauna for Sauna card`() {
    val apresCard = apresCard { name = "Sauna" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Sauna::class.java)
  }

  @Test
  fun `create returns Bar for Bar card`() {
    val apresCard = apresCard { name = "Bar" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Bar::class.java)
  }

  @Test
  fun `create returns Dining for Dining card`() {
    val apresCard = apresCard { name = "Dining" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Dining::class.java)
  }

  @Test
  fun `create returns Village for Village card`() {
    val apresCard = apresCard { name = "Village" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Village::class.java)
  }

  @Test
  fun `create returns Massage for Massage card`() {
    val apresCard = apresCard { name = "Massage" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Massage::class.java)
  }

  @Test
  fun `create returns Journal for Journal card`() {
    val apresCard = apresCard { name = "Journal" }
    val apres = apresFactory.create(apresCard)
    assertThat(apres).isInstanceOf(Journal::class.java)
  }
}
