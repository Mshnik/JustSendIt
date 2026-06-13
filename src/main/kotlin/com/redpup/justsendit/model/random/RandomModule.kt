package com.redpup.justsendit.model.random

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Binding module for [Random] and its dependencies. */
class RandomModule : KtAbstractModule() {
  override fun configure() {
    bind<Random>().to<RandomImpl>()
  }

  @Provides
  @Singleton
  fun javaRandom(): java.util.Random = java.util.Random()
}