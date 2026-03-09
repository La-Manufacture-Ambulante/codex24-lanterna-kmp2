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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.ansi

import com.googlecode.lanterna.input.KeyStroke
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

abstract class UnixLikeTerminal protected constructor(
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
    private val terminalCtrlCBehaviour: CtrlCBehaviour?,
) : ANSITerminal(terminalInput, terminalOutput, terminalCharset) {
    enum class CtrlCBehaviour {
        TRAP,
        CTRL_C_KILLS_APPLICATION,
    }

    private val catchSpecialCharacters: Boolean
    private val shutdownHook: Thread
    private var acquired = false

    init {
        val catchSpecialCharactersPropValue =
            System.getProperty("com.googlecode.lanterna.terminal.UnixTerminal.catchSpecialCharacters", "")
        catchSpecialCharacters = !"false".equals(catchSpecialCharactersPropValue.trim().lowercase())
        shutdownHook = object : Thread("Lanterna STTY restore") {
            override fun run() {
                exitPrivateModeAndRestoreState()
            }
        }
        acquire()
    }

    @Throws(IOException::class)
    protected open fun acquire() {
        onResized(80, 24)
        saveTerminalSettings()
        canonicalMode(false)
        keyEchoEnabled(false)
        if (catchSpecialCharacters) {
            keyStrokeSignalsEnabled(false)
        }
        registerTerminalResizeListener(
            Runnable {
                try {
                    terminalSize
                } catch (_: IOException) {
                }
            },
        )
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        acquired = true
    }

    @Throws(IOException::class)
    override fun close() {
        exitPrivateModeAndRestoreState()
        Runtime.getRuntime().removeShutdownHook(shutdownHook)
        acquired = false
        super.close()
    }

    @Throws(IOException::class)
    override fun pollInput(): KeyStroke? {
        val key = super.pollInput()
        isCtrlC(key)
        return key
    }

    @Throws(IOException::class)
    override fun readInput(): KeyStroke {
        val key = super.readInput()
        isCtrlC(key)
        return key
    }

    protected fun getTerminalCtrlCBehaviour(): CtrlCBehaviour? {
        return terminalCtrlCBehaviour
    }

    @Throws(IOException::class)
    protected abstract fun registerTerminalResizeListener(onResize: Runnable?)

    @Throws(IOException::class)
    protected abstract fun saveTerminalSettings()

    @Throws(IOException::class)
    protected abstract fun restoreTerminalSettings()

    @Throws(IOException::class)
    private fun restoreTerminalSettingsAndKeyStrokeSignals() {
        restoreTerminalSettings()
        if (catchSpecialCharacters) {
            keyStrokeSignalsEnabled(true)
        }
    }

    @Throws(IOException::class)
    protected abstract fun keyEchoEnabled(enabled: Boolean)

    @Throws(IOException::class)
    protected abstract fun canonicalMode(enabled: Boolean)

    @Throws(IOException::class)
    protected abstract fun keyStrokeSignalsEnabled(enabled: Boolean)

    @Throws(IOException::class)
    private fun isCtrlC(key: KeyStroke?) {
        if (
            key != null &&
            terminalCtrlCBehaviour == CtrlCBehaviour.CTRL_C_KILLS_APPLICATION &&
            key.character != null &&
            key.character == 'c' &&
            !key.isAltDown &&
            key.isCtrlDown
        ) {
            if (isInPrivateMode()) {
                exitPrivateMode()
            }
            System.exit(1)
        }
    }

    private fun exitPrivateModeAndRestoreState() {
        if (!acquired) {
            return
        }
        try {
            if (isInPrivateMode()) {
                exitPrivateMode()
            }
        } catch (_: IOException) {
        } catch (_: IllegalStateException) {
        }

        try {
            restoreTerminalSettingsAndKeyStrokeSignals()
        } catch (_: IOException) {
        }
    }
}
