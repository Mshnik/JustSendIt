package com.redpup.justsendit.view.sidebar

import com.redpup.justsendit.model.GameModel
import javafx.scene.control.Tab
import javafx.scene.control.TabPane

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import com.redpup.justsendit.view.skill.AccordionCardList
import javafx.application.Platform

import com.redpup.justsendit.model.player.Player
import com.redpup.justsendit.view.player.OpponentWidget

/**
 * Right sidebar hub containing Shop, Log, Trash, and Opponents.
 */
class SidebarHub(private val gameModel: GameModel, private val logPanel: LogPanel) : TabPane(), Logger {

  private val shopTab = Tab("SHOP")
  private val logTab = Tab("LOG")
  private val trashTab = Tab("TRASH")
  
  val shopList = AccordionCardList()
  private val trashList = AccordionCardList()
  
  private val opponentTabs = mutableMapOf<Player, Tab>()

  init {
    this.styleClass.add("sidebar-hub")
    this.tabClosingPolicy = TabClosingPolicy.UNAVAILABLE
    this.prefWidth = 300.0

    setupShop()
    setupLog()
    setupTrash()

    tabs.addAll(shopTab, logTab, trashTab)
    
    update()
  }

  override fun log(log: Log) {
    Platform.runLater { update() }
  }

  private fun setupShop() {
    shopTab.content = shopList
  }

  private fun setupLog() {
    logTab.content = logPanel
  }

  private fun setupTrash() {
    trashTab.content = trashList
  }
  
  fun update() {
    shopList.setCards(gameModel.shop.keys.toList())
    val currentPlayer = gameModel.currentPlayer
    trashList.setCards(currentPlayer.skillDiscard.toList())
    
    // Update opponent tabs
    gameModel.players.filter { it != currentPlayer }.forEach { opponent ->
        val tab = opponentTabs.getOrPut(opponent) {
            Tab(opponent.name).also { 
                it.content = OpponentWidget(opponent)
                tabs.add(it)
            }
        }
        (tab.content as OpponentWidget).update()
    }
    
    // Remove tabs for players that might have left (if applicable)
    val opponents = gameModel.players.filter { it != currentPlayer }.toSet()
    val toRemove = opponentTabs.keys.filter { it !in opponents }
    toRemove.forEach { 
        tabs.remove(opponentTabs[it])
        opponentTabs.remove(it)
    }
  }
}
