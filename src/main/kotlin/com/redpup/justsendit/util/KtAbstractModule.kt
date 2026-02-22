package com.redpup.justsendit.util

import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder

/** Extension of [AbstractModule] that exposes improved syntax. */
abstract class KtAbstractModule : AbstractModule() {

  /** Override for [bind] that allows using diamond notation. */
  protected inline fun <reified T> bind(): LinkedBindingBuilder<T> {
    return bind(Key.get(T::class.java))
  }

  /** Override for [to] that allows using diamond notation. */
  protected inline fun <reified T> LinkedBindingBuilder<in T>.to(): ScopedBindingBuilder {
    return to(Key.get(T::class.java))
  }
}