package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.O_RDONLY
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.isatty
import platform.posix.open
import platform.posix.system
import platform.posix.winsize

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalRuntime {
    private val ttyFd: Int by lazy {
        open("/dev/tty", O_RDONLY)
    }

    private fun activeTerminalFd(): Int {
        return resolveTerminalFdForTest(
            stdinIsTty = isatty(STDIN_FILENO) == 1,
            stdoutIsTty = isatty(STDOUT_FILENO) == 1,
            ttyFd = ttyFd,
        )
    }

    actual fun queryTerminalSize(
        fallbackColumns: Int,
        fallbackRows: Int,
    ): PosixTerminalDimensions {
        val size = readWinsize()
        return if (size != null) {
            PosixTerminalDimensions(columns = size.first, rows = size.second)
        } else {
            PosixTerminalDimensions(columns = fallbackColumns, rows = fallbackRows)
        }
    }

    actual fun configureRawModeNoEcho(): Boolean {
        if (activeTerminalFd() < 0) {
            return false
        }
        val command = rawModeCommandForTest(stdinIsTty = isatty(STDIN_FILENO) == 1)
        return system(command) == 0
    }

    actual fun restoreCookedMode(): Boolean {
        if (activeTerminalFd() < 0) {
            return false
        }
        val command = cookedModeCommandForTest(stdinIsTty = isatty(STDIN_FILENO) == 1)
        return system(command) == 0
    }

    private fun readWinsize(): Pair<Int, Int>? =
        memScoped {
            val ws = alloc<winsize>()
            val fd =
                if (isatty(STDOUT_FILENO) == 1) {
                    STDOUT_FILENO
                } else {
                    activeTerminalFd()
                }
            if (fd < 0) {
                return@memScoped null
            }
            val result = ioctl(fd, TIOCGWINSZ.toULong(), ws.ptr)
            if (result == 0) {
                Pair(ws.ws_col.toInt(), ws.ws_row.toInt())
            } else {
                null
            }
        }
}

internal fun resolveTerminalFdForTest(
    stdinIsTty: Boolean,
    stdoutIsTty: Boolean,
    ttyFd: Int,
): Int {
    if (stdinIsTty) {
        return STDIN_FILENO
    }
    if (stdoutIsTty) {
        return STDOUT_FILENO
    }
    return ttyFd
}

internal fun rawModeCommandForTest(stdinIsTty: Boolean): String {
    return if (stdinIsTty) {
        "stty raw -echo >/dev/null 2>&1"
    } else {
        "stty raw -echo </dev/tty >/dev/null 2>&1"
    }
}

internal fun cookedModeCommandForTest(stdinIsTty: Boolean): String {
    return if (stdinIsTty) {
        "stty sane >/dev/null 2>&1"
    } else {
        "stty sane </dev/tty >/dev/null 2>&1"
    }
}
