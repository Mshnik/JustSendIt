package com.redpup.justsendit.simulation

import com.google.inject.Provides
import com.google.inject.Singleton
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.log.LoggerInstance
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.log.PrintlineLogger
import com.redpup.justsendit.model.GameModelModule
import com.redpup.justsendit.model.random.RandomModule
import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.util.SystemTimeSourceModule

import com.redpup.justsendit.control.ai.RandomAiController
import com.redpup.justsendit.control.ai.RiskyAiController
import com.redpup.justsendit.control.ai.SimpleAiController

/** Guice module for simulation mode. */
class SimulationModule : KtAbstractModule() {
  override fun configure() {
    install(GameModelModule())
    install(RandomModule())
    install(SystemTimeSourceModule())
    install(LoggerModule(LoggerInstance(PrintlineLogger())))
  }

  @Provides
  @Singleton
  fun providePlayerControllers(): List<PlayerController> {
    return listOf(
      SimpleAiController("AI-Simple"),
      RandomAiController("AI-Random"),
      RiskyAiController("AI-Safe", 0.1),
      RiskyAiController("AI-Balanced", 0.5),
      RiskyAiController("AI-Risky", 0.9)
    )
  }
}
