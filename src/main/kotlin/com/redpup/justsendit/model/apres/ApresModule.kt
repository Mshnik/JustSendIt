package com.redpup.justsendit.model.apres

import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the apres deck. */
class ApresModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresFactory>().to<ApresFactoryImpl>()
  }
}