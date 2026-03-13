package com.googlecode.lanterna.terminal.nativeposix

expect object PosixTerminalRuntime {
    fun queryTerminalSize(
        fallbackColumns: Int = 80,
        fallbackRows: Int = 24,
    ): PosixTerminalDimensions

    fun configureRawModeNoEcho(): Boolean

    fun restoreCookedMode(): Boolean
}
