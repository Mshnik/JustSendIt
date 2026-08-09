package com.redpup.justsendit.util

import kotlin.math.pow
import kotlin.math.round

/** Rounds this to [decimals] places. */
fun Float.round(decimals: Int): Float {
  val multiplier = 10.0f.pow(decimals)
  return round(this * multiplier) / multiplier
}

/** Rounds this to [decimals] places. */
fun Double.round(decimals: Int): Double {
  val multiplier = 10.0.pow(decimals)
  return round(this * multiplier) / multiplier
}
