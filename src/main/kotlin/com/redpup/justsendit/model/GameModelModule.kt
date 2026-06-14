package com.redpup.justsendit.model

import com.redpup.justsendit.log.LoggerClass
import com.redpup.justsendit.log.LoggerModule
import com.redpup.justsendit.log.PrintlineLogger
import com.redpup.justsendit.model.apres.ApresModule
import com.redpup.justsendit.model.player.PlayerModule
import com.redpup.justsendit.model.random.RandomModule
import com.redpup.justsendit.model.skill.SkillModule
import com.redpup.justsendit.model.supply.SupplyModule
import com.redpup.justsendit.util.KtAbstractModule

/** Top level module that installs all game model dependencies. */
class GameModelModule : KtAbstractModule() {
  override fun configure() {
    install(ApresModule())
    install(PlayerModule())
    install(RandomModule())
    install(SkillModule())
    install(SupplyModule())
    install(LoggerModule(LoggerClass(PrintlineLogger::class)))

    bind<GameModel>().to<MutableGameModel>()
  }
}