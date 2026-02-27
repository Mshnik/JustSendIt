package com.redpup.justsendit.log

import com.google.inject.multibindings.Multibinder
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.util.KtAbstractModule

/** Logging access through different means of logging. */
interface Logger {
  /** Logs the given [log]. */
  fun log(log: Log)
}

/** A logger that logs to the console. */
class PrintlineLogger : Logger {
  override fun log(log: Log) {
    println(log)
  }
}
