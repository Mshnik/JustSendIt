package com.redpup.justsendit.util

/** Extension methods for functions. */
object FunctionExtensions {
  /** Chains [then] onto [this]. */
  fun <T, R1, R2> ((T) -> R1).then(then: ((R1) -> R2)): ((T) -> R2) = { t -> then(this(t)) }

  /**
   * Chains [then] onto [this] if the input is non-null.
   * Returns null immediately if the input is null.
   */
  fun <T, R1, R2> ((T) -> R1?).thenNonNull(then: ((R1) -> R2)): ((T) -> R2?) =
    { t ->
      val r = this(t)
      if (r != null) then(r) else null
    }

  /** Chains a default [orElse] onto this. */
  fun <T, R> ((T) -> R?).orElse(orElse: R) = { t: T -> this(t) ?: orElse }
}