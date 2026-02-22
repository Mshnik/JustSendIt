package com.redpup.justsendit.model

import com.redpup.justsendit.model.apres.ApresModule
import com.redpup.justsendit.model.player.PlayerModule
import com.redpup.justsendit.model.supply.SupplyModule
import com.redpup.justsendit.util.KtAbstractModule

/** Top level module that installs all game model dependencies. */
class GameModelModule : KtAbstractModule() {
  override fun configure() {
    install(ApresModule())
    install(PlayerModule())
    install(SupplyModule())

    bind<GameModel>().to<MutableGameModel>()
  }
}