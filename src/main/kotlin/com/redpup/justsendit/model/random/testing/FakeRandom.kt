package com.redpup.justsendit.model.random.testing

import com.redpup.justsendit.model.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A fake instance of [Random].
 *
 * By default, all randoms return the default value unless configured.
 */
@Singleton
class FakeRandom @Inject constructor() : Random {
  private val nextIntValues = mutableMapOf<Int, MutableList<Int>>()

  override fun nextInt(bound: Int): Int = nextIntValues[bound]?.removeFirst() ?: 0

  override fun shuffle(iterable: Iterable<*>) {}

  /** Adds an entry for [nextInt]: The next time [bound] is requested, [value] is returned. */
  fun addNextInt(bound: Int, value: Int) {
    if (nextIntValues[value] == null) {
      nextIntValues[value] = mutableListOf()
    }
    nextIntValues[bound]!!.add(value)
  }

  /** Clears all prepared values. */
  fun reset() {
    nextIntValues.clear()
  }
}