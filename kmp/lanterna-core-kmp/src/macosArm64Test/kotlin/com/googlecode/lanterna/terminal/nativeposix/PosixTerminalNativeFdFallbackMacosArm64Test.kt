package com.googlecode.lanterna.terminal.nativeposix

import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import kotlin.test.Test
import kotlin.test.assertEquals

class PosixTerminalNativeFdFallbackMacosArm64Test {
    @Test
    fun inputFdUsesStdinWhenStdinIsTty() {
        assertEquals(STDIN_FILENO, resolveInputFdForTest(stdinIsTty = true, ttyFd = 42))
    }

    @Test
    fun inputFdFallsBackToControllingTtyWhenStdinIsDetached() {
        assertEquals(42, resolveInputFdForTest(stdinIsTty = false, ttyFd = 42))
    }

    @Test
    fun terminalFdPrefersStdinThenStdoutThenTty() {
        assertEquals(
            STDIN_FILENO,
            resolveTerminalFdForTest(stdinIsTty = true, stdoutIsTty = true, ttyFd = 77),
        )
        assertEquals(
            STDOUT_FILENO,
            resolveTerminalFdForTest(stdinIsTty = false, stdoutIsTty = true, ttyFd = 77),
        )
        assertEquals(
            77,
            resolveTerminalFdForTest(stdinIsTty = false, stdoutIsTty = false, ttyFd = 77),
        )
    }

    @Test
    fun rawAndCookedModeCommandsUseDevTtyWhenStdinIsDetached() {
        assertEquals(
            "stty raw -echo </dev/tty >/dev/null 2>&1",
            rawModeCommandForTest(stdinIsTty = false),
        )
        assertEquals(
            "stty sane </dev/tty >/dev/null 2>&1",
            cookedModeCommandForTest(stdinIsTty = false),
        )
    }
}
