package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.cards.friday.*
import com.redpup.justsendit.model.player.cards.saturday.*
import com.redpup.justsendit.model.player.cards.sunday.*
import com.redpup.justsendit.model.player.proto.playerCard
import kotlin.reflect.KClass
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PlayerFactoryTest {

  private val factory = PlayerFactoryImpl()

  @ParameterizedTest
  @MethodSource("getPlayerCardData")
  fun `creates correct player card type`(data: Pair<String, KClass<out PlayerCard>>) {
    val (name, type) = data
    val card = playerCard { this.name = name }
    val playerCard = factory.create(card)
    assertThat(playerCard).isInstanceOf(type.java)
  }

  companion object {
    @JvmStatic
    fun getPlayerCardData(): List<Pair<String, KClass<out PlayerCard>>> {
      return listOf(
        "George" to George::class,
        "Jenny" to Jenny::class,
        "Michael" to Michael::class,
        "Andy" to Andy::class,
        "James" to James::class,
        "David" to David::class,
        "Dannver" to Dannver::class,
        "Yifei" to Yifei::class,
        "Courtney" to Courtney::class,

        "Reckless" to Reckless::class,
        "Methodical" to Methodical::class,
        "Swift" to Swift::class,
        "Calm" to Calm::class,
        "Precise" to Precise::class,
        "Relentless" to Relentless::class,
        "Bold" to Bold::class,
        "Rowdy" to Rowdy::class,
        "Determined" to Determined::class,

        "Wild" to Wild::class,
        "Tough" to Tough::class,
        "Steadfast" to Steadfast::class,
        "Nimble" to Nimble::class,
        "Fluid" to Fluid::class,
        "Rigorous" to Rigorous::class,
        "Classic" to Classic::class,
        "Rugged" to Rugged::class,
        "Dazzling" to Dazzling::class,
      )
    }
  }
}
