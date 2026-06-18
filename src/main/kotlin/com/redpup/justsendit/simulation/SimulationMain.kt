package com.redpup.justsendit.simulation

import com.google.inject.Guice
import kotlinx.coroutines.runBlocking

/** Main entry point for the simulation. */
object SimulationMain {
  @JvmStatic
  fun main(args: Array<String>) {
    val injector = Guice.createInjector(SimulationModule())
    val runner = injector.getInstance(SimulationRunner::class.java)

    runBlocking {
      runner.run()
    }
  }
}
