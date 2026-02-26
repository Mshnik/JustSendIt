package com.redpup.justsendit.util

import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class ListExtensionsTest {

  @Test
  fun `pop removes and returns first element`() {
    val list = mutableListOf(1, 2, 3)
    val popped = list.pop()
    assertThat(popped).isEqualTo(1)
    assertThat(list).containsExactly(2, 3).inOrder()
  }

  @Test
  fun `pop throws exception on empty list`() {
    val list = mutableListOf<Int>()
    assertFailsWith<NoSuchElementException> {
      list.pop()
    }
  }
}
