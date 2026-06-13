package com.redpup.justsendit.model.supply.testing

import com.google.common.annotations.VisibleForTesting
import com.redpup.justsendit.model.supply.ApresDeck
import com.redpup.justsendit.model.supply.PlayerDeck
import com.redpup.justsendit.model.supply.SkillDeck
import com.redpup.justsendit.model.supply.StarterDeck
import com.redpup.justsendit.model.supply.ShopDeck
import com.redpup.justsendit.model.supply.TileSupply
import com.redpup.justsendit.util.KtAbstractModule

/** Test binding module for the supply section of the game. */
@VisibleForTesting
class FakeSupplyModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresDeck>().to<FakeApresDeck>()
    bind<PlayerDeck>().to<FakePlayerDeck>()
    bind<TileSupply>().to<FakeTileSupply>()

    bind<SkillDeck>().to<FakeSkillDeck>()
    bind(SkillDeck::class.java).annotatedWith(StarterDeck::class.java).to(FakeSkillDeck::class.java)
    bind(SkillDeck::class.java).annotatedWith(ShopDeck::class.java).to(FakeSkillDeck::class.java)
  }
}
