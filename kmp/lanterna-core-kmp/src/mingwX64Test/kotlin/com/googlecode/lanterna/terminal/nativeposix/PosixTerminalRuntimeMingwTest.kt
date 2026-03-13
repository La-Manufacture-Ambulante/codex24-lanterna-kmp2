package com.googlecode.lanterna.terminal.nativeposix

import kotlin.test.Test
import kotlin.test.assertTrue

class PosixTerminalRuntimeMingwTest {
    @Test
    fun queryTerminalSizeReturnsPositiveDimensions() {
        val size = PosixTerminalRuntime.queryTerminalSize(fallbackColumns = 120, fallbackRows = 40)
        assertTrue(size.columns > 0)
        assertTrue(size.rows > 0)
    }

    @Test
    fun rawModeRoundTripDoesNotThrow() {
        val rawEnabled = PosixTerminalRuntime.configureRawModeNoEcho()
        if (rawEnabled) {
            assertTrue(PosixTerminalRuntime.restoreCookedMode())
        }
    }
}
