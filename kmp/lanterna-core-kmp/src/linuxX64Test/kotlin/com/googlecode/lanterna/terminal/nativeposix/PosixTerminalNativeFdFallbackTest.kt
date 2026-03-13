package com.googlecode.lanterna.terminal.nativeposix

import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PosixTerminalNativeFdFallbackTest {
    @Test
    fun inputFdUsesStdinWhenStdinIsTty() {
        assertEquals(STDIN_FILENO, resolveInputFdForTest(stdinIsTty = true, ttyFd = 42))
    }

    @Test
    fun inputFdUsesStdinWhenStdinIsDetached() {
        assertEquals(STDIN_FILENO, resolveInputFdForTest(stdinIsTty = false, ttyFd = 42))
    }

    @Test
    fun inputCandidatesUseStdinOnly() {
        assertContentEquals(
            intArrayOf(STDIN_FILENO),
            resolveInputCandidatesForTest(stdinIsTty = true, ttyFd = 42),
        )
        assertContentEquals(
            intArrayOf(STDIN_FILENO),
            resolveInputCandidatesForTest(stdinIsTty = true, ttyFd = STDIN_FILENO),
        )
        assertContentEquals(
            intArrayOf(STDIN_FILENO),
            resolveInputCandidatesForTest(stdinIsTty = false, ttyFd = 42),
        )
        assertContentEquals(
            intArrayOf(STDIN_FILENO),
            resolveInputCandidatesForTest(stdinIsTty = false, ttyFd = -1),
        )
    }

    @Test
    fun blockingInputFdUsesStdinOnly() {
        assertEquals(STDIN_FILENO, resolveBlockingInputFdForTest(stdinIsTty = true, ttyFd = 42))
        assertEquals(STDIN_FILENO, resolveBlockingInputFdForTest(stdinIsTty = false, ttyFd = 42))
        assertEquals(STDIN_FILENO, resolveBlockingInputFdForTest(stdinIsTty = false, ttyFd = -1))
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
    fun rawAndCookedModeCommandsUseStdinOnly() {
        assertEquals(
            "stty raw -echo >/dev/null 2>&1",
            rawModeCommandForTest(stdinIsTty = false),
        )
        assertEquals(
            "stty sane >/dev/null 2>&1",
            cookedModeCommandForTest(stdinIsTty = false),
        )
    }
}
