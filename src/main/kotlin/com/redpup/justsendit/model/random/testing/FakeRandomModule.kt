package com.redpup.justsendit.model.random.testing

import com.google.inject.Provides
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.util.KtAbstractModule
import javax.inject.Singleton

/** Binding module for [Random] and its dependencies. */
class FakeRandomModule : KtAbstractModule() {
  override fun configure() {
    bind<Random>().to<FakeRandom>()
  }

  @Provides
  @Singleton
  fun javaRandom(): java.util.Random = java.util.Random()
}