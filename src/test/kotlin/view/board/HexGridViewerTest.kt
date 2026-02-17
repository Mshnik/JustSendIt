package com.redpup.justsendit.view.board

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class HexGridViewerTest {

  @Test
  fun `init method initializes GameModel without error`() {
    // This is a limited test because HexGridViewer is a JavaFX Application.
    // We are not launching the UI, but we can test the init() method,
    // which contains logic for model initialization.

    // To run this test, the JavaFX toolkit must be initialized.
    // This might require specific test runner configurations (e.g., using TestFX).
    // For now, we attempt to run it and expect it to fail if the environment
    // is not set up for JavaFX testing.

    // A simple way to initialize the toolkit for a test run if it's not already running.
    try {
      com.sun.javafx.application.PlatformImpl.startup {}
    } catch (e: Exception) {
      // Platform may already be running, which is fine.
    }

    val viewer = HexGridViewer()

    assertDoesNotThrow {
      viewer.init()
    }
  }
}
