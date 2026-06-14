package com.redpup.justsendit.log

import com.redpup.justsendit.log.proto.Log

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

/** A wrapping logger that logs to the given [logger]. */
class LazyForwardingLogger(private val logger: () -> Logger) : Logger {
  override fun log(log: Log) {
    logger().log(log)
  }
}
