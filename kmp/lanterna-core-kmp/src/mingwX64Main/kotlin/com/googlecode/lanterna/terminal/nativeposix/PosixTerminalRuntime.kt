package com.googlecode.lanterna.terminal.nativeposix

actual object PosixTerminalRuntime {
    actual fun queryTerminalSize(
        fallbackColumns: Int,
        fallbackRows: Int,
    ): PosixTerminalDimensions {
        return PosixTerminalDimensions(columns = fallbackColumns, rows = fallbackRows)
    }

    actual fun configureRawModeNoEcho(): Boolean {
        return false
    }

    actual fun restoreCookedMode(): Boolean {
        return false
    }
}
