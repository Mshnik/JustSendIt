package com.redpup.justsendit.model.apres

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.apres.cards.*
import com.redpup.justsendit.model.apres.proto.apresCard
import kotlin.reflect.KClass
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ApresFactoryTest {

  private val factory = ApresFactoryImpl()

  @ParameterizedTest
  @MethodSource("getApresCardData")
  fun `creates correct apres card type`(data: Pair<String, KClass<out Apres>>) {
    val (name, type) = data
    val card = apresCard { this.name = name }
    val apres = factory.create(card)
    assertThat(apres).isInstanceOf(type.java)
  }

  companion object {
    @JvmStatic
    fun getApresCardData(): List<Pair<String, KClass<out Apres>>> {
      return listOf(
        "Bar" to Bar::class,
        "Concert" to Concert::class,
        "Dining" to Dining::class,
        "Dog Sledding" to DogSledding::class,
        "Fire Pit" to FirePit::class,
        "Fireworks" to Fireworks::class,
        "Ice Skating" to IceSkating::class,
        "Karaoke" to Karaoke::class,
        "Lodge" to Lodge::class,
        "Massage" to Massage::class,
        "Photo-Op" to PhotoOp::class,
        "Sauna" to Sauna::class
      )
    }
  }
}
