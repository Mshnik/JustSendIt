package com.redpup.justsendit.model.supply

import com.google.inject.Provides
import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the supply section of the game. */
class SupplyModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresDeck>().to<ApresDeckImpl>()
    bind<PlayerDeck>().to<PlayerDeckImpl>()
    bind<SkillDecks>().to<SkillDecksInstance>()
    bind<TileSupply>().to<TileSupplyImpl>()
  }

  @Provides
  @ApresPath
  fun apresPath(): String = "src/main/resources/com/redpup/justsendit/model/apres/apres.textproto"

  @Provides
  @PlayerPath
  fun playerPath(): String =
    "src/main/resources/com/redpup/justsendit/model/players/players.textproto"

  @Provides
  @TilePath
  fun tilePath(): String =
    "src/main/resources/com/redpup/justsendit/model/board/tile/tiles.textproto"

  @Provides
  @LocationPath
  fun locationPath(): String =
    "src/main/resources/com/redpup/justsendit/model/board/tile/tile_locations.textproto"
}