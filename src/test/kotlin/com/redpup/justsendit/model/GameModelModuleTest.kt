package com.redpup.justsendit.model

import com.google.inject.Guice
import com.google.inject.Provides
import com.redpup.justsendit.control.PlayerController
import com.redpup.justsendit.util.KtAbstractModule
import com.redpup.justsendit.util.SystemTimeSourceModule
import org.junit.Test

class GameModelModuleTest {
  @Test
  fun bindsDependencies() {
    Guice.createInjector(
      GameModelModule(),
      SystemTimeSourceModule(),
      object : KtAbstractModule() {
        @Provides
        fun playerControllers(): List<@JvmSuppressWildcards PlayerController> =
          listOf()
      }
    ).injectMembers(GameModel::class)
  }
}