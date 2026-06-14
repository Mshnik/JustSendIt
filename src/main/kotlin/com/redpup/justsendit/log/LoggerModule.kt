package com.redpup.justsendit.log

import com.google.inject.Provider
import com.google.inject.multibindings.Multibinder
import com.redpup.justsendit.util.KtAbstractModule
import kotlin.reflect.KClass

/** Means of passing in a logger. */
sealed interface LoggerWrapper
data class LoggerClass(val clazz: KClass<out Logger>) : LoggerWrapper
data class LoggerInstance(val instance: Logger) : LoggerWrapper
data class LoggerProvider(val provider: Provider<Logger>) : LoggerWrapper

/** A module that binds zero or more loggers. */
class LoggerModule(private vararg val loggers: LoggerWrapper) : KtAbstractModule() {
  override fun configure() {
    val multiBinder = Multibinder.newSetBinder(binder(), Logger::class.java)
    loggers.forEach {
      when (it) {
        is LoggerClass -> multiBinder.addBinding().to(it.clazz.java)
        is LoggerInstance -> multiBinder.addBinding().toInstance(it.instance)
        is LoggerProvider -> multiBinder.addBinding().toProvider(it.provider)
      }
    }
  }
}
