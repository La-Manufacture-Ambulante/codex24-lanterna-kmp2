package com.googlecode.lanterna.terminal.win32

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.BasicCharacterPattern
import com.googlecode.lanterna.input.CharacterPattern
import com.googlecode.lanterna.input.KeyDecodingProfile
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.googlecode.lanterna.terminal.win32.WinDef.CONSOLE_SCREEN_BUFFER_INFO
import com.sun.jna.platform.win32.Wincon as JnaWincon
import com.sun.jna.ptr.IntByReference
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.ArrayList

class WindowsTerminal @Throws(IOException::class) constructor(
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
    terminalCtrlCBehaviour: UnixLikeTerminal.CtrlCBehaviour?,
) : UnixLikeTerminal(terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {
    private var settings: IntArray? = null

    @Throws(IOException::class)
    constructor() : this(
        CONSOLE_INPUT,
        CONSOLE_OUTPUT,
        CONSOLE_CHARSET,
        UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION,
    )

    init {
        CONSOLE_INPUT.onWindowBufferSizeEvent { evt ->
            onResized(evt.dwSize.X.toInt(), evt.dwSize.Y.toInt())
        }
    }

    override fun getDefaultKeyDecodingProfile(): KeyDecodingProfile {
        val keyDecodingProfile = ArrayList<CharacterPattern>()
        keyDecodingProfile.add(BasicCharacterPattern(KeyStroke(KeyType.ENTER), '\r'))
        keyDecodingProfile.addAll(super.getDefaultKeyDecodingProfile().patterns)
        return object : KeyDecodingProfile {
            override val patterns: Collection<CharacterPattern> = keyDecodingProfile
        }
    }

    @Throws(IOException::class)
    override fun acquire() {
        super.acquire()

        var terminalOutputMode = consoleOutputMode
        terminalOutputMode = terminalOutputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_PROCESSING
        terminalOutputMode = terminalOutputMode or Wincon.DISABLE_NEWLINE_AUTO_RETURN
        Wincon.INSTANCE.SetConsoleMode(CONSOLE_OUTPUT.getHandle(), terminalOutputMode)

        var terminalInputMode = consoleInputMode
        terminalInputMode = terminalInputMode or JnaWincon.ENABLE_MOUSE_INPUT
        terminalInputMode = terminalInputMode or JnaWincon.ENABLE_WINDOW_INPUT
        terminalInputMode = terminalInputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_INPUT
        Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), terminalInputMode)
    }

    @Throws(IOException::class)
    override fun saveTerminalSettings() {
        settings = intArrayOf(consoleInputMode, consoleOutputMode)
    }

    @Throws(IOException::class)
    override fun restoreTerminalSettings() {
        if (settings != null) {
            Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), settings!![0])
            Wincon.INSTANCE.SetConsoleMode(CONSOLE_OUTPUT.getHandle(), settings!![1])
        }
    }

    @Throws(IOException::class)
    override fun keyEchoEnabled(enabled: Boolean) {
        var mode = consoleInputMode
        mode = if (enabled) mode or JnaWincon.ENABLE_ECHO_INPUT else mode and JnaWincon.ENABLE_ECHO_INPUT.inv()
        Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
    }

    @Throws(IOException::class)
    override fun canonicalMode(enabled: Boolean) {
        var mode = consoleInputMode
        mode = if (enabled) mode or JnaWincon.ENABLE_LINE_INPUT else mode and JnaWincon.ENABLE_LINE_INPUT.inv()
        Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
    }

    @Throws(IOException::class)
    override fun keyStrokeSignalsEnabled(enabled: Boolean) {
        var mode = consoleInputMode
        mode = if (enabled) mode or JnaWincon.ENABLE_PROCESSED_INPUT else mode and JnaWincon.ENABLE_PROCESSED_INPUT.inv()
        Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
    }

    override fun findTerminalSize(): TerminalSize {
        val screenBufferInfo = CONSOLE_SCREEN_BUFFER_INFO()
        Wincon.INSTANCE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT.getHandle(), screenBufferInfo)
        val columns = screenBufferInfo.srWindow.Right - screenBufferInfo.srWindow.Left + 1
        val rows = screenBufferInfo.srWindow.Bottom - screenBufferInfo.srWindow.Top + 1
        return TerminalSize(columns.toInt(), rows.toInt())
    }

    @Throws(IOException::class)
    override fun registerTerminalResizeListener(onResize: Runnable?) {
        // ignore
    }

    override var cursorPosition: TerminalPosition?
        @Throws(IOException::class)
        get() {
            val screenBufferInfo = CONSOLE_SCREEN_BUFFER_INFO()
            Wincon.INSTANCE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT.getHandle(), screenBufferInfo)
            val column = screenBufferInfo.dwCursorPosition.X - screenBufferInfo.srWindow.Left
            val row = screenBufferInfo.dwCursorPosition.Y - screenBufferInfo.srWindow.Top
            return TerminalPosition(column.toInt(), row.toInt())
        }
        @Throws(IOException::class)
        set(position) {
            super.cursorPosition = position
        }

    private val consoleInputMode: Int
        get() {
            val mode = IntByReference()
            Wincon.INSTANCE.GetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
            return mode.value
        }

    private val consoleOutputMode: Int
        get() {
            val mode = IntByReference()
            Wincon.INSTANCE.GetConsoleMode(CONSOLE_OUTPUT.getHandle(), mode)
            return mode.value
        }

    companion object {
        private val CONSOLE_CHARSET: Charset = StandardCharsets.UTF_8
        private val CONSOLE_INPUT = WindowsConsoleInputStream(CONSOLE_CHARSET)
        private val CONSOLE_OUTPUT = WindowsConsoleOutputStream(CONSOLE_CHARSET)
    }
}
