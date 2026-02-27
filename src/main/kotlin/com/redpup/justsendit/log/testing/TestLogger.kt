package com.redpup.justsendit.log.testing

import com.google.common.annotations.VisibleForTesting
import com.google.inject.Singleton
import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Inject

/** A testing logger. */
@VisibleForTesting
@Singleton
class TestLogger @Inject constructor() : Logger {
  val logs = mutableListOf<Log>()

  fun reset() {
    logs.clear()
  }

  override fun log(log: Log) {
    logs.add(log)
  }
}

/** Testing logger module. */
@VisibleForTesting
class TestLoggerModule : KtAbstractModule() {
  override fun configure() {
    install(LoggerModule(TestLogger::class))
  }
}