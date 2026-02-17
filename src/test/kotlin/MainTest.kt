package com.redpup.justsendit

import org.junit.jupiter.api.Test

class MainTest {

    @Test
    fun `main function runs without throwing exceptions`() {
        // A simple test to ensure the main function can be executed without any runtime errors.
        // It doesn't validate the output, but serves as a basic smoke test.
        try {
            main()
        } catch (e: Exception) {
            // We can't use assertDoesNotThrow because it's not available in the JUnit version used.
            // So we fail the test if any exception is caught.
            throw AssertionError("main() should not throw any exception, but it threw ${e::class.java.simpleName}", e)
        }
    }
}
