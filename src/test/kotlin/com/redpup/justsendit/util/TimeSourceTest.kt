package com.redpup.justsendit.util

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlin.test.Test

class TimeSourceTest {

  @Test
  fun `SystemTimeSource returns current time`() {
    val now = Instant.now()
    val sourceNow = SystemTimeSource.now()
    assertThat(sourceNow).isAtLeast(now)
  }
}
