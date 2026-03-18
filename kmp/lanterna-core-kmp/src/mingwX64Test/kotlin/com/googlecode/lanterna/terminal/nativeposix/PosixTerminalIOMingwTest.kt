package com.googlecode.lanterna.terminal.nativeposix

import kotlin.test.Test
import kotlin.test.assertTrue

class PosixTerminalIOMingwTest {
    @Test
    fun ioOperationsAreCallable() {
        val hasInput = PosixTerminalIO.hasInput(0)
        assertTrue(hasInput || !hasInput)

        PosixTerminalIO.writeByte('\n'.code)
        PosixTerminalIO.write("lanterna")
        PosixTerminalIO.writeBytes("kmp".encodeToByteArray())
        PosixTerminalIO.flush()
    }
}
