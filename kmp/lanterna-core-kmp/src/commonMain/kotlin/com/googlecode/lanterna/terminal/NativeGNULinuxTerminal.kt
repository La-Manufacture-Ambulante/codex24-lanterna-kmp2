/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.sun.jna.Native
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Terminal implementation that uses native libraries
 */
open class NativeGNULinuxTerminal : UnixLikeTerminal {
    private val libc: PosixLibC
    private var savedTerminalState: PosixLibC.termios? = null

    @Throws(IOException::class)
    constructor() : this(
        System.`in`,
        System.out,
        Charset.defaultCharset(),
        CtrlCBehaviour.CTRL_C_KILLS_APPLICATION
    )

    @Throws(IOException::class)
    constructor(
        terminalInput: InputStream,
        terminalOutput: OutputStream,
        terminalCharset: Charset,
        terminalCtrlCBehaviour: CtrlCBehaviour
    ) : super(
        terminalInput,
        terminalOutput,
        terminalCharset,
        terminalCtrlCBehaviour
    ) {
        this.libc = Native.loadLibrary("c", PosixLibC::class.java) as PosixLibC
        this.savedTerminalState = null
    }

    @Throws(IOException::class)
    fun saveTerminalSettings() {
        savedTerminalState = getTerminalState()
    }

    @Throws(IOException::class)
    fun restoreTerminalSettings() {
        if (savedTerminalState != null) {
            libc.tcsetattr(PosixLibC.STDIN_FILENO, PosixLibC.TCSANOW, savedTerminalState)
        }
    }

    @Throws(IOException::class)
    fun keyEchoEnabled(b: Boolean) {
        val state = getTerminalState()
        if (b) {
            state.c_lflag = state.c_lflag or PosixLibC.ECHO
        } else {
            state.c_lflag = state.c_lflag and PosixLibC.ECHO.inv()
        }
        libc.tcsetattr(PosixLibC.STDIN_FILENO, PosixLibC.TCSANOW, state)
    }

    @Throws(IOException::class)
    fun canonicalMode(b: Boolean) {
        val state = getTerminalState()
        if (b) {
            state.c_lflag = state.c_lflag or PosixLibC.ICANON
        } else {
            state.c_lflag = state.c_lflag and PosixLibC.ICANON.inv()
        }
        libc.tcsetattr(PosixLibC.STDIN_FILENO, PosixLibC.TCSANOW, state)
    }

    @Throws(IOException::class)
    fun keyStrokeSignalsEnabled(b: Boolean) {
        val state = getTerminalState()
        if (b) {
            state.c_lflag = state.c_lflag or PosixLibC.ISIG
        } else {
            state.c_lflag = state.c_lflag and PosixLibC.ISIG.inv()
        }
        libc.tcsetattr(PosixLibC.STDIN_FILENO, PosixLibC.TCSANOW, state)
    }

    @Throws(IOException::class)
    fun registerTerminalResizeListener(runnable: Runnable) {
        libc.signal(PosixLibC.SIGWINCH, object : PosixLibC.sig_t() {
            @Synchronized
            override fun invoke(signal: Int) {
                runnable.run()
            }
        })
    }

    @Throws(IOException::class)
    override protected fun findTerminalSize(): TerminalSize {
        val winsize = PosixLibC.winsize()
        libc.ioctl(PosixLibC.STDOUT_FILENO, PosixLibC.TIOCGWINSZ, winsize)
        return TerminalSize(winsize.ws_col, winsize.ws_row)
    }

    private fun getTerminalState(): PosixLibC.termios {
        val termios = PosixLibC.termios()
        libc.tcgetattr(PosixLibC.STDIN_FILENO, termios)
        return termios
    }
}
