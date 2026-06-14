package com.redpup.justsendit.view

import com.redpup.justsendit.model.GameModel
import com.redpup.justsendit.view.log.LogPanel
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

import com.redpup.justsendit.log.Logger
import com.redpup.justsendit.log.proto.Log
import javafx.application.Platform

/**
 * Right sidebar hub containing Shop, Log, and Trash.
 */
class SidebarHub(private val gameModel: GameModel, private val logPanel: LogPanel) : TabPane(), Logger {

  private val shopTab = Tab("SHOP")
  private val logTab = Tab("LOG")
  private val trashTab = Tab("TRASH")
  
  private val shopList = AccordionCardList()
  private val trashList = AccordionCardList()

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
    trashList.setCards(gameModel.currentPlayer.skillDiscard.toList())
  }
}
