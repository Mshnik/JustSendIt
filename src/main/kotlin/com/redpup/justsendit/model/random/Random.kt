package com.redpup.justsendit.model.random

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Inject
import javax.inject.Singleton

/** Wrapper on random operations in the game. */
interface Random {
  /** Returns a random value in range [0, bound). */
  fun nextInt(bound: Int): Int
}

/** Default impl of Random. */
@Singleton
class RandomImpl @Inject constructor(private val random: java.util.Random) : Random {
  override fun nextInt(bound: Int): Int = random.nextInt(bound)
}

