package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.isatty
import platform.posix.system
import platform.posix.winsize

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalRuntime {
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
        if (isatty(STDIN_FILENO) != 1) {
            return false
        }
        return system("stty raw -echo >/dev/null 2>&1") == 0
    }

    actual fun restoreCookedMode(): Boolean {
        if (isatty(STDIN_FILENO) != 1) {
            return false
        }
        return system("stty sane >/dev/null 2>&1") == 0
    }

    private fun readWinsize(): Pair<Int, Int>? =
        memScoped {
            val ws = alloc<winsize>()
            val result = ioctl(STDOUT_FILENO, TIOCGWINSZ.toULong(), ws.ptr)
            if (result == 0) {
                Pair(ws.ws_col.toInt(), ws.ws_row.toInt())
            } else {
                null
            }
        }
}
