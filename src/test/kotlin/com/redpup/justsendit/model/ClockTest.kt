package com.redpup.justsendit.model

import com.google.common.truth.Truth.assertThat
import com.redpup.justsendit.model.proto.Day
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ClockTest {

  @Test
  fun `MutableClock default values`() {
    val clock = MutableClock()
    assertThat(clock.round).isEqualTo(1)
    assertThat(clock.turn).isEqualTo(1)
    assertThat(clock.day).isEqualTo(Day.DAY_FRIDAY)
    assertThat(clock.subTurn).isEqualTo(1)
    assertThat(clock.isFirstSubTurn).isTrue()
  }

  @Test
  fun `MutableClock maxRound depends on day`() {
    assertThat(MutableClock(day = Day.DAY_FRIDAY).maxRound).isEqualTo(9)
    assertThat(MutableClock(day = Day.DAY_SATURDAY).maxRound).isEqualTo(8)
    assertThat(MutableClock(day = Day.DAY_SUNDAY).maxRound).isEqualTo(7)
    assertThrows(IllegalArgumentException::class.java) {
      MutableClock(day = Day.DAY_UNSET).maxRound
    }
  }

  @Test
  fun `advanceSubTurn increments subTurn`() {
    val clock = MutableClock()
    clock.advanceSubTurn()
    assertThat(clock.subTurn).isEqualTo(2)
    assertThat(clock.isFirstSubTurn).isFalse()
  }

  @Test
  fun `advanceTurn increments turn and resets subTurn`() {
    val clock = MutableClock()
    clock.advanceSubTurn()
    clock.advanceTurn()
    assertThat(clock.turn).isEqualTo(2)
    assertThat(clock.subTurn).isEqualTo(1)
  }

  @Test
  fun `advanceRound increments round and resets turn and subTurn`() {
    val clock = MutableClock()
    clock.advanceTurn()
    clock.advanceSubTurn()
    clock.advanceRound()
    assertThat(clock.round).isEqualTo(2)
    assertThat(clock.turn).isEqualTo(1)
    assertThat(clock.subTurn).isEqualTo(1)
  }

  @Test
  fun `advanceDay advances day, resets round, turn, and subTurn`() {
    val clock = MutableClock(round = 5, day = Day.DAY_FRIDAY)
    clock.advanceSubTurn()
    clock.advanceDay()
    assertThat(clock.day).isEqualTo(Day.DAY_SATURDAY)
    assertThat(clock.round).isEqualTo(1)
    assertThat(clock.turn).isEqualTo(1)
    assertThat(clock.subTurn).isEqualTo(1)

    clock.advanceDay()
    assertThat(clock.day).isEqualTo(Day.DAY_SUNDAY)

    assertThrows(IllegalStateException::class.java) {
      clock.advanceDay()
    }
  }
}
