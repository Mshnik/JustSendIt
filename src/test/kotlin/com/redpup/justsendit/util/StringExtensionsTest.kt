package com.redpup.justsendit.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StringExtensionsTest {

  @Test
  fun `toTitleCase with lowercase string`() {
    assertThat("hello".toTitleCase()).isEqualTo("Hello")
  }

  @Test
  fun `toTitleCase with uppercase string`() {
    assertThat("WORLD".toTitleCase()).isEqualTo("World")
  }

  @Test
  fun `toTitleCase with mixed case string`() {
    assertThat("GoOdByE".toTitleCase()).isEqualTo("Goodbye")
  }
}
