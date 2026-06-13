package com.redpup.justsendit.model.player

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.player.cards.PlayerCard
import com.redpup.justsendit.model.player.proto.playerCard
import kotlin.reflect.KClass
import kotlin.test.Ignore
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PlayerFactoryTest {

  private val factory = PlayerFactoryImpl()

  @ParameterizedTest
  @MethodSource("getPlayerCardData")
  @Ignore // TODO
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
        // TODO
      )
    }
  }
}
