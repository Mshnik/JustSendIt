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

  @Test
  fun `count duplicates returns 0 for empty list`() {
    assertThat(listOf<Int>().countDuplicates()).isEqualTo(0)
  }

  @Test
  fun `count duplicates returns 0 for 1 element list`() {
    assertThat(listOf(1).countDuplicates()).isEqualTo(0)
  }

  @Test
  fun `count duplicates returns 0 for list with no duplicates`() {
    assertThat(listOf(1, 2, 3).countDuplicates()).isEqualTo(0)
  }

  @Test
  fun `count duplicates returns 1 for list with 1 pair`() {
    assertThat(listOf(1, 2, 3, 1).countDuplicates()).isEqualTo(1)
  }

  @Test
  fun `count duplicates returns 2 for list with 2 pairs`() {
    assertThat(listOf(1, 2, 3, 1, 2).countDuplicates()).isEqualTo(2)
  }

  @Test
  fun `count duplicates returns 3 for list with 1 triplet`() {
    assertThat(listOf(1, 2, 3, 1, 1).countDuplicates()).isEqualTo(3)
  }
}
