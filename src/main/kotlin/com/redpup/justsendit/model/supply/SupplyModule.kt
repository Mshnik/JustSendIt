package com.redpup.justsendit.model.supply

import com.google.inject.Provides
import com.google.inject.Singleton
import com.redpup.justsendit.model.random.Random
import com.redpup.justsendit.model.skill.SkillFactory
import com.redpup.justsendit.util.KtAbstractModule

/** Binding module for the supply section of the game. */
class SupplyModule : KtAbstractModule() {
  override fun configure() {
    bind<ApresDeck>().to<ApresDeckImpl>()
    bind<PlayerDeck>().to<PlayerDeckImpl>()
    bind<TileSupply>().to<TileSupplyImpl>()
  }

  @Provides
  @StarterDeck
  @Singleton
  fun starterDeck(
    skillFactory: SkillFactory,
    random: Random,
  ): SkillDeck =
    SkillDeckInstance(
      "src/main/resources/com/redpup/justsendit/model/shop/skill/Starter.textproto",
      random,
      skillFactory
    )

  @Provides
  @ShopDeck
  @Singleton
  fun shopDeck(skillFactory: SkillFactory, random: Random): SkillDeck =
    SkillDeckInstance(
      "src/main/resources/com/redpup/justsendit/model/shop/skill/Shop.textproto",
      random,
      skillFactory
    )

  @Provides
  @SpecialDeck
  @Singleton
  fun specialDeck(skillFactory: SkillFactory, random: Random): SkillDeck =
    SkillDeckInstance(
      "src/main/resources/com/redpup/justsendit/model/shop/skill/Special.textproto",
      random,
      skillFactory
    )

  @Provides
  @ApresPath
  fun apresPath(): String = "src/main/resources/com/redpup/justsendit/model/shop/apres.textproto"

  @Provides
  @PlayerPath
  fun playerPath(): String =
    "src/main/resources/com/redpup/justsendit/model/shop/players.textproto"

  @Provides
  @TilePath
  fun tilePath(): String =
    "src/main/resources/com/redpup/justsendit/model/board/tile/mountain_tiles.textproto"

  @Provides
  @LocationPath
  fun locationPath(): String =
    "src/main/resources/com/redpup/justsendit/model/board/tile/tile_locations.textproto"
}