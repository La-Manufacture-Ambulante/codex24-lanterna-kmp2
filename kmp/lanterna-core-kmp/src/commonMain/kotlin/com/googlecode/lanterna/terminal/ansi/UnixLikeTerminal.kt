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

/**
 * Base class for all terminals that generally behave like Unix terminals. This class defined a number of abstract
 * methods that needs to be implemented which are all used to setup the terminal environment (turning off echo,
 * canonical mode, etc) and also a control variable for how to react to CTRL+c keystroke.
 */
abstract class UnixLikeTerminal protected constructor(
    terminalInput: InputStream?,
    terminalOutput: OutputStream?,
    terminalCharset: Charset?,
    private val terminalCtrlCBehaviour: CtrlCBehaviour,
) : ANSITerminal(terminalInput, terminalOutput, terminalCharset) {
    /**
     * This enum lets you control how Lanterna will handle a ctrl+c keystroke from the user.
     */
    enum class CtrlCBehaviour {
        /**
         * Pressing ctrl+c doesn't kill the application, it will be added to the input queue as any other key stroke
         */
        TRAP,

        /**
         * Pressing ctrl+c will restore the terminal and kill the application as it normally does with terminal
         * applications. Lanterna will restore the terminal and then call `System.exit(1)` for this.
         */
        CTRL_C_KILLS_APPLICATION,
    }

    private val catchSpecialCharacters: Boolean
    private val shutdownHook: Thread
    private var acquired = false

    init {
        val catchSpecialCharactersPropValue =
            System.getProperty("com.googlecode.lanterna.terminal.UnixTerminal.catchSpecialCharacters", "")
        catchSpecialCharacters = !"false".equals(catchSpecialCharactersPropValue.trim().lowercase())
        shutdownHook =
            object : Thread("Lanterna STTY restore") {
                override fun run() {
                    exitPrivateModeAndRestoreState()
                }
            }
        acquire()
    }

    @Throws(IOException::class)
    protected open fun acquire() {
        // Make sure to set an initial size
        onResized(80, 24)
        saveTerminalSettings()
        canonicalMode(false)
        keyEchoEnabled(false)
        if (catchSpecialCharacters) {
            keyStrokeSignalsEnabled(false)
        }
        registerTerminalResizeListener(
            Runnable {
                // This will trigger a resize notification as the size will be different than before
                try {
                    terminalSize
                } catch (_: IOException) {
                    // Not much to do here, we can't re-throw it
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

    protected fun getTerminalCtrlCBehaviour(): CtrlCBehaviour {
        return terminalCtrlCBehaviour
    }

    @Throws(IOException::class)
    protected abstract fun registerTerminalResizeListener(onResize: Runnable)

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
