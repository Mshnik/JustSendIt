package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.model.supply.SkillDecks
import com.redpup.justsendit.model.supply.TileSupply
import com.redpup.justsendit.util.KtAbstractModule

/** Test binding module for the supply section of the game. */
@VisibleForTesting
class FakeSupplyModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresDeck>().to<FakeApresDeck>()
    bind<PlayerDeck>().to<FakePlayerDeck>()
    bind<SkillDecks>().to<FakeSkillDecks>()
    bind<TileSupply>().to<FakeTileSupply>()
  }
}