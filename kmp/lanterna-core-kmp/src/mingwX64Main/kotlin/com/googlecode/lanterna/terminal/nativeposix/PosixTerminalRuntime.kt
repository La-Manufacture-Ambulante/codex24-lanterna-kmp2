package com.googlecode.lanterna.terminal.nativeposix

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVarOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.getenv
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO
import platform.windows.DISABLE_NEWLINE_AUTO_RETURN
import platform.windows.ENABLE_ECHO_INPUT
import platform.windows.ENABLE_EXTENDED_FLAGS
import platform.windows.ENABLE_LINE_INPUT
import platform.windows.ENABLE_MOUSE_INPUT
import platform.windows.ENABLE_PROCESSED_INPUT
import platform.windows.ENABLE_QUICK_EDIT_MODE
import platform.windows.ENABLE_VIRTUAL_TERMINAL_INPUT
import platform.windows.ENABLE_VIRTUAL_TERMINAL_PROCESSING
import platform.windows.ENABLE_WINDOW_INPUT
import platform.windows.GetConsoleMode
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.STD_INPUT_HANDLE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.SetConsoleMode

@OptIn(ExperimentalForeignApi::class)
actual object PosixTerminalRuntime {
    private var savedInputMode: UInt? = null
    private var savedOutputMode: UInt? = null

    actual fun queryTerminalSize(
        fallbackColumns: Int,
        fallbackRows: Int,
    ): PosixTerminalDimensions {
        val win32Size = readConsoleWindowSize()
        if (win32Size != null) {
            return PosixTerminalDimensions(columns = win32Size.first, rows = win32Size.second)
        }
        val columns = systemEnvInt("COLUMNS") ?: fallbackColumns
        val rows = systemEnvInt("LINES") ?: fallbackRows
        return PosixTerminalDimensions(columns = columns, rows = rows)
    }

    actual fun configureRawModeNoEcho(): Boolean {
        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt()) ?: return false
        val outputHandle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt()) ?: return false
        memScoped {
            val inputMode = alloc<UIntVarOf<UInt>>()
            val outputMode = alloc<UIntVarOf<UInt>>()
            if (GetConsoleMode(inputHandle, inputMode.ptr) == 0) {
                return false
            }
            if (GetConsoleMode(outputHandle, outputMode.ptr) == 0) {
                return false
            }
            val previousInputMode = inputMode.value
            val previousOutputMode = outputMode.value
            savedInputMode = previousInputMode
            savedOutputMode = previousOutputMode

            val rawInputMode =
                inputMode.value and
                    ENABLE_ECHO_INPUT.toUInt().inv() and
                    ENABLE_LINE_INPUT.toUInt().inv() and
                    ENABLE_PROCESSED_INPUT.toUInt().inv()
            val vtInputMode =
                (rawInputMode or
                    ENABLE_EXTENDED_FLAGS.toUInt() or
                    ENABLE_MOUSE_INPUT.toUInt() or
                    ENABLE_WINDOW_INPUT.toUInt() or
                    ENABLE_VIRTUAL_TERMINAL_INPUT.toUInt()) and
                    ENABLE_QUICK_EDIT_MODE.toUInt().inv()
            if (SetConsoleMode(inputHandle, vtInputMode) == 0) {
                savedInputMode = null
                savedOutputMode = null
                return false
            }

            val vtOutputMode =
                outputMode.value or
                    ENABLE_VIRTUAL_TERMINAL_PROCESSING.toUInt() or
                    DISABLE_NEWLINE_AUTO_RETURN.toUInt()
            if (SetConsoleMode(outputHandle, vtOutputMode) == 0) {
                SetConsoleMode(inputHandle, previousInputMode)
                savedInputMode = null
                savedOutputMode = null
                return false
            }
        }
        return true
    }

    actual fun restoreCookedMode(): Boolean {
        val inputMode = savedInputMode
        val outputMode = savedOutputMode
        if (inputMode == null || outputMode == null) {
            return false
        }
        val inputHandle = GetStdHandle(STD_INPUT_HANDLE.toUInt()) ?: return false
        val outputHandle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt()) ?: return false
        val inputRestored = SetConsoleMode(inputHandle, inputMode) != 0
        val outputRestored = SetConsoleMode(outputHandle, outputMode) != 0
        if (inputRestored && outputRestored) {
            savedInputMode = null
            savedOutputMode = null
        }
        return inputRestored && outputRestored
    }

    private fun readConsoleWindowSize(): Pair<Int, Int>? =
        memScoped {
            val outputHandle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt()) ?: return null
            val info = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
            if (GetConsoleScreenBufferInfo(outputHandle, info.ptr) == 0) {
                return null
            }
            val columns = (info.srWindow.Right - info.srWindow.Left + 1).toInt()
            val rows = (info.srWindow.Bottom - info.srWindow.Top + 1).toInt()
            if (columns <= 0 || rows <= 0) {
                null
            } else {
                Pair(columns, rows)
            }
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun systemEnvInt(name: String): Int? {
    val value = getenv(name)?.toKString() ?: return null
    val parsed = value.toIntOrNull() ?: return null
    return if (parsed > 0) parsed else null
}
