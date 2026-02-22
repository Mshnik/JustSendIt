package com.redpup.justsendit.model.apres.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.util.KtAbstractModule

/** Testing Binding module for the apres deck. */
@VisibleForTesting
class FakeApresModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresFactory>().to<FakeApresFactory>()
  }
}