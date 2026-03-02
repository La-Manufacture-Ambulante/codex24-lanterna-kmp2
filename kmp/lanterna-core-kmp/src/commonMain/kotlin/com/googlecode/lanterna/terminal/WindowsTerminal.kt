package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Terminal implementation for the regular Windows cmd.exe terminal emulator, using native invocations through jna to
 * interact with it.
 */
open class WindowsTerminal @Throws(IOException::class) constructor(
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
    terminalCtrlCBehaviour: CtrlCBehaviour?
) : UnixLikeTerminal(
    terminalInput,
    terminalOutput,
    terminalCharset,
    terminalCtrlCBehaviour
) {

    private var savedTerminalInputMode: Int? = null
    private var savedTerminalOutputMode: Int? = null

    @Throws(IOException::class)
    constructor() : this(
        System.`in`,
        System.out,
        Charset.defaultCharset(),
        CtrlCBehaviour.CTRL_C_KILLS_APPLICATION
    )

    @Throws(IOException::class)
    override fun acquire() {
        super.acquire()
        var terminalOutputMode = getConsoleOutputMode()
        terminalOutputMode = terminalOutputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_PROCESSING
        terminalOutputMode = terminalOutputMode or Wincon.DISABLE_NEWLINE_AUTO_RETURN
        WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_OUTPUT_HANDLE, terminalOutputMode)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun saveTerminalSettings() {
        this.savedTerminalInputMode = getConsoleInputMode()
        this.savedTerminalOutputMode = getConsoleOutputMode()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun restoreTerminalSettings() {
        if (savedTerminalInputMode != null) {
            WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_INPUT_HANDLE, savedTerminalInputMode!!)
            WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_OUTPUT_HANDLE, savedTerminalOutputMode!!)
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun keyEchoEnabled(enabled: Boolean) {
        var mode = getConsoleInputMode()
        if (enabled) {
            mode = mode or Wincon.ENABLE_ECHO_INPUT
        } else {
            mode = mode and Wincon.ENABLE_ECHO_INPUT.inv()
        }
        WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun canonicalMode(enabled: Boolean) {
        var mode = getConsoleInputMode()
        if (enabled) {
            mode = mode or Wincon.ENABLE_LINE_INPUT
        } else {
            mode = mode and Wincon.ENABLE_LINE_INPUT.inv()
        }
        WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun keyStrokeSignalsEnabled(enabled: Boolean) {
        var mode = getConsoleInputMode()
        if (enabled) {
            mode = mode or Wincon.ENABLE_PROCESSED_INPUT
        } else {
            mode = mode and Wincon.ENABLE_PROCESSED_INPUT.inv()
        }
        WINDOWS_CONSOLE.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
    }

    @Throws(IOException::class)
    override fun findTerminalSize(): TerminalSize {
        val screenBufferInfo = WinDef.CONSOLE_SCREEN_BUFFER_INFO()
        WINDOWS_CONSOLE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT_HANDLE, screenBufferInfo)
        val columns = screenBufferInfo.srWindow.Right - screenBufferInfo.srWindow.Left + 1
        val rows = screenBufferInfo.srWindow.Bottom - screenBufferInfo.srWindow.Top + 1
        return TerminalSize(columns, rows)
    }

    @Throws(IOException::class)
    override fun registerTerminalResizeListener(runnable: Runnable?) {
        // Not implemented yet
    }

    @Synchronized
    open fun getCursorPosition(): TerminalPosition {
        val screenBufferInfo = WinDef.CONSOLE_SCREEN_BUFFER_INFO()
        WINDOWS_CONSOLE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT_HANDLE, screenBufferInfo)
        val column = screenBufferInfo.dwCursorPosition.X - screenBufferInfo.srWindow.Left
        val row = screenBufferInfo.dwCursorPosition.Y - screenBufferInfo.srWindow.Top
        return TerminalPosition(column, row)
    }

    private fun getConsoleInputMode(): Int {
        val lpMode = IntByReference()
        WINDOWS_CONSOLE.GetConsoleMode(CONSOLE_INPUT_HANDLE, lpMode)
        return lpMode.value
    }

    private fun getConsoleOutputMode(): Int {
        val lpMode = IntByReference()
        WINDOWS_CONSOLE.GetConsoleMode(CONSOLE_OUTPUT_HANDLE, lpMode)
        return lpMode.value
    }

    companion object {
        private val WINDOWS_CONSOLE: Wincon =
            Native.loadLibrary("kernel32", Wincon::class.java, W32APIOptions.UNICODE_OPTIONS) as Wincon
        private val CONSOLE_INPUT_HANDLE: WinDef.HANDLE =
            WINDOWS_CONSOLE.GetStdHandle(Wincon.STD_INPUT_HANDLE)
        private val CONSOLE_OUTPUT_HANDLE: WinDef.HANDLE =
            WINDOWS_CONSOLE.GetStdHandle(Wincon.STD_OUTPUT_HANDLE)
    }
}
