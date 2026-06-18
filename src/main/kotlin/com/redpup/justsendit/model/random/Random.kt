package com.redpup.justsendit.model.random

import com.google.errorprone.annotations.DoNotMock
import javax.inject.Inject
import javax.inject.Singleton

/** Wrapper on random operations in the game. */
@DoNotMock(value = "Use FakeRandom instead.")
interface Random {
  /** Returns a random value in range [0, bound). */
  fun nextInt(bound: Int): Int

  /** Shuffles the given [iterable]. */
  fun shuffle(iterable: Iterable<*>)

  companion object {
    /** Shuffles [this] using [random]. */
    fun Iterable<*>.shuffle(random: Random) {
      random.shuffle(this)
    }
  }
}

/** Default impl of Random. */
@Singleton
class RandomImpl @Inject constructor(private val random: java.util.Random) : Random {
  override fun nextInt(bound: Int): Int = random.nextInt(bound)

  override fun shuffle(iterable: Iterable<*>) {
    iterable.shuffled(random)
  }
}

