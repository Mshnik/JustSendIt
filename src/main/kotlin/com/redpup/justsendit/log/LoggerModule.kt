package com.redpup.justsendit.log

import com.google.inject.multibindings.Multibinder
import com.redpup.justsendit.util.KtAbstractModule
import kotlin.reflect.KClass

/** A module that binds zero or more loggers. */
class LoggerModule(private vararg val loggers: KClass<out Logger>) : KtAbstractModule() {
  override fun configure() {
    val multiBinder = Multibinder.newSetBinder(binder(), Logger::class.java)
    loggers.forEach { multiBinder.addBinding().to(it.java) }
  }
}
