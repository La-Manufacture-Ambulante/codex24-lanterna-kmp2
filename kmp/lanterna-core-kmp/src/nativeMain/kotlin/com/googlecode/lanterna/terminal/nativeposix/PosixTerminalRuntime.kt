package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.STDOUT_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.system
import platform.posix.winsize

@OptIn(ExperimentalForeignApi::class)
object PosixTerminalRuntime {
    fun queryTerminalSize(
        fallbackColumns: Int = 80,
        fallbackRows: Int = 24,
    ): PosixTerminalDimensions {
        val size = readWinsize()
        return if (size != null) {
            PosixTerminalDimensions(columns = size.first, rows = size.second)
        } else {
            PosixTerminalDimensions(columns = fallbackColumns, rows = fallbackRows)
        }
    }

    fun configureRawModeNoEcho(): Boolean = system("stty raw -echo") == 0

    fun restoreCookedMode(): Boolean = system("stty sane") == 0

    private fun readWinsize(): Pair<Int, Int>? = memScoped {
        val ws = alloc<winsize>()
        val result = ioctl(STDOUT_FILENO, TIOCGWINSZ.toULong(), ws.ptr)
        if (result == 0) {
            Pair(ws.ws_col.toInt(), ws.ws_row.toInt())
        } else {
            null
        }
    }
}
