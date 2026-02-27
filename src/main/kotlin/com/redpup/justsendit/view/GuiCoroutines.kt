package com.redpup.justsendit.view

import com.redpup.justsendit.util.KtAbstractModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


// 1. Define a Scope for your UI (e.g., in your Controller)
private val uiScope = object : CoroutineScope {
  override val coroutineContext = SupervisorJob() + Dispatchers.Main
}

/** A binding module for [uiScope]. */
class GuiCoroutineModule : KtAbstractModule() {
  override fun configure() {
    bind<CoroutineScope>().toInstance(uiScope)
  }
}