package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.windows.GetStdHandle
import platform.windows.STD_INPUT_HANDLE

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalRuntime {
    private var rawModeEnabled: Boolean = false

    actual fun queryTerminalSize(
        fallbackColumns: Int,
        fallbackRows: Int,
    ): PosixTerminalDimensions {
        val columns = systemEnvInt("COLUMNS") ?: fallbackColumns
        val rows = systemEnvInt("LINES") ?: fallbackRows
        return PosixTerminalDimensions(columns = columns, rows = rows)
    }

    actual fun configureRawModeNoEcho(): Boolean {
        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt()) ?: return false
        rawModeEnabled = inputHandle != null
        return rawModeEnabled
    }

    actual fun restoreCookedMode(): Boolean {
        val wasEnabled = rawModeEnabled
        rawModeEnabled = false
        return wasEnabled
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun systemEnvInt(name: String): Int? {
    val value = getenv(name)?.toKString() ?: return null
    val parsed = value.toIntOrNull() ?: return null
    return if (parsed > 0) parsed else null
}
