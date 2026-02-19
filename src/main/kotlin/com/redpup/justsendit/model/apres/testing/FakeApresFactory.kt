package com.redpup.justsendit.model.apres.testing

import com.redpup.justsendit.model.apres.Apres
import com.redpup.justsendit.model.apres.ApresFactory
import com.redpup.justsendit.model.apres.proto.ApresCard

/** A testing implementation of [ApresFactory] */
class FakeApresFactory : ApresFactory {
  override val factories: MutableMap<String, (ApresCard) -> Apres> = mutableMapOf()
}