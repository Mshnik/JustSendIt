package com.redpup.justsendit.model.random

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.proto.Die
import com.redpup.justsendit.model.random.Dice.downgrade
import com.redpup.justsendit.model.random.Dice.roll
import com.redpup.justsendit.model.random.Dice.upgrade
import com.redpup.justsendit.model.random.testing.FakeRandom
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DiceTest {

  private val random = FakeRandom()

  @Test
  fun `roll dice`() {
    random.addNextInt(4, 0) // roll 1
    assertThat(Die.DIE_GREEN.roll(random)).isEqualTo(1)

    random.addNextInt(6, 5) // roll 6
    assertThat(Die.DIE_BLUE.roll(random)).isEqualTo(6)

    random.addNextInt(8, 7) // roll 8
    assertThat(Die.DIE_BLACK.roll(random)).isEqualTo(8)
  }

  @Test
  fun `roll unset die throws`() {
    assertThrows(IllegalArgumentException::class.java) {
      Die.DIE_UNSET.roll(random)
    }
  }

  @Test
  fun `upgrade dice`() {
    assertThat(Die.DIE_GREEN.upgrade()).isEqualTo(Die.DIE_BLUE)
    assertThat(Die.DIE_BLUE.upgrade()).isEqualTo(Die.DIE_BLACK)
    assertThat(Die.DIE_BLACK.upgrade()).isEqualTo(Die.DIE_BLACK)
  }

  @Test
  fun `downgrade dice`() {
    assertThat(Die.DIE_BLACK.downgrade()).isEqualTo(Die.DIE_BLUE)
    assertThat(Die.DIE_BLUE.downgrade()).isEqualTo(Die.DIE_GREEN)
    assertThat(Die.DIE_GREEN.downgrade()).isEqualTo(Die.DIE_GREEN)
  }
}
