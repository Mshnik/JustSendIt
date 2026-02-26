package com.redpup.justsendit.util

import java.time.Instant

/** A supplier of timestamps. */
interface TimeSource {
  fun now(): Instant
}

/** The system timesource. */
object SystemTimeSource : TimeSource {
  override fun now(): Instant = Instant.now()
}

/** A module providing [TimeSource]. */
class SystemTimeSourceModule : KtAbstractModule() {
  override fun configure() {
    bind<TimeSource>().toInstance(SystemTimeSource)
  }
}