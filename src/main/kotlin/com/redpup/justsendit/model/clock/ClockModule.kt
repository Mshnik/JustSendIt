package com.redpup.justsendit.model.clock

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for [Clock]. */
class ClockModule(private val maxRound: Int = 6) : KtAbstractModule() {
  override fun configure() {
    bind<Clock>().to<ClockImpl>()
  }

  @Provides
  @MaxRound
  fun provideMaxRound(): Int = maxRound
}