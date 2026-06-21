package com.redpup.justsendit.view

import javafx.scene.Node

/** Extension functions for working with GUI elements. */
object GuiExt {
  /** Adds [style] to this and returns this. */
  fun <T : Node> T.withStyle(style: String): T {
    styleClass.add(style)
    return this
  }
}