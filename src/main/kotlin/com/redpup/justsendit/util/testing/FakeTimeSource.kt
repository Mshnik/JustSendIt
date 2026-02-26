package com.redpup.justsendit.util.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.util.TimeSource
import java.time.Duration
import java.time.Instant

/** A testing implementation of [TimeSource]. */
@VisibleForTesting
class FakeTimeSource(
  var now: Instant = Instant.ofEpochMilli(12345),
  var autoAdvance: Duration = Duration.ZERO,
) : TimeSource {
  override fun now(): Instant {
    val n = now
    now += autoAdvance
    return n
  }
}

/** A testing module of [FakeTimeSource]. */
@VisibleForTesting
class FakeTimeSourceModule(var now: Instant = Instant.ofEpochMilli(12345)) : KtAbstractModule() {
  private val fakeTimeSource = FakeTimeSource(now)

  override fun configure() {
    bind<TimeSource>().toInstance(fakeTimeSource)
  }
}
