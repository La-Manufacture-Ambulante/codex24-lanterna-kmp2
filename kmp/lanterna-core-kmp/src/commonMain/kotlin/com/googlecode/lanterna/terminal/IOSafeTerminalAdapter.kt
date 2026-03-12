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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import java.io.IOException
import java.util.concurrent.TimeUnit

open class IOSafeTerminalAdapter internal constructor(
    private val backend: Terminal,
    internal val exceptionHandler: ExceptionHandler,
) : IOSafeTerminal {
    internal interface ExceptionHandler {
        fun onException(e: IOException)
    }

    private class ConvertToRuntimeException : ExceptionHandler {
        override fun onException(e: IOException) {
            throw RuntimeException(e)
        }
    }

    private class DoNothingAndOrReturnNull : ExceptionHandler {
        override fun onException(e: IOException) = Unit
    }

    override var cursorPosition: TerminalPosition?
        get() =
            try {
                backend.cursorPosition
            } catch (e: IOException) {
                exceptionHandler.onException(e)
                null
            }
        set(position) {
            try {
                backend.cursorPosition = position
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

    override val terminalSize: TerminalSize?
        get() =
            try {
                backend.terminalSize
            } catch (e: IOException) {
                exceptionHandler.onException(e)
                null
            }

    override fun enterPrivateMode() {
        try {
            backend.enterPrivateMode()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun exitPrivateMode() {
        try {
            backend.exitPrivateMode()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun clearScreen() {
        try {
            backend.clearScreen()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun setCursorPosition(
        x: Int,
        y: Int,
    ) {
        try {
            backend.setCursorPosition(x, y)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun setCursorVisible(visible: Boolean) {
        try {
            backend.setCursorVisible(visible)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun putCharacter(c: Char) {
        try {
            backend.putCharacter(c)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun putString(string: String?) {
        try {
            backend.putString(string)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun newTextGraphics(): TextGraphics? {
        return try {
            backend.newTextGraphics()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
            null
        }
    }

    override fun enableSGR(sgr: SGR?) {
        try {
            backend.enableSGR(sgr)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun disableSGR(sgr: SGR?) {
        try {
            backend.disableSGR(sgr)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun resetColorAndSGR() {
        try {
            backend.resetColorAndSGR()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun setForegroundColor(color: TextColor?) {
        try {
            backend.setForegroundColor(color)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun setBackgroundColor(color: TextColor?) {
        try {
            backend.setBackgroundColor(color)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun addResizeListener(listener: TerminalResizeListener?) {
        backend.addResizeListener(listener)
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        backend.removeResizeListener(listener)
    }

    override fun enquireTerminal(
        timeout: Int,
        timeoutUnit: TimeUnit?,
    ): ByteArray? {
        return try {
            backend.enquireTerminal(timeout, timeoutUnit)
        } catch (e: IOException) {
            exceptionHandler.onException(e)
            null
        }
    }

    override fun bell() {
        try {
            backend.bell()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun flush() {
        try {
            backend.flush()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun close() {
        try {
            backend.close()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
        }
    }

    override fun pollInput(): KeyStroke? {
        return try {
            backend.pollInput()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
            null
        }
    }

    override fun readInput(): KeyStroke? {
        return try {
            backend.readInput()
        } catch (e: IOException) {
            exceptionHandler.onException(e)
            null
        }
    }

    class Extended internal constructor(
        private val backend: ExtendedTerminal,
        exceptionHandler: ExceptionHandler,
    ) : IOSafeTerminalAdapter(backend, exceptionHandler), IOSafeExtendedTerminal {
        override fun setTerminalSize(
            columns: Int,
            rows: Int,
        ) {
            try {
                backend.setTerminalSize(columns, rows)
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun setTitle(title: String?) {
            try {
                backend.setTitle(title)
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun pushTitle() {
            try {
                backend.pushTitle()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun popTitle() {
            try {
                backend.popTitle()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun iconify() {
            try {
                backend.iconify()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun deiconify() {
            try {
                backend.deiconify()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun maximize() {
            try {
                backend.maximize()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun unmaximize() {
            try {
                backend.unmaximize()
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun setMouseCaptureMode(mouseCaptureMode: MouseCaptureMode?) {
            try {
                backend.setMouseCaptureMode(mouseCaptureMode)
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }

        override fun scrollLines(
            firstLine: Int,
            lastLine: Int,
            distance: Int,
        ) {
            try {
                backend.scrollLines(firstLine, lastLine, distance)
            } catch (e: IOException) {
                exceptionHandler.onException(e)
            }
        }
    }

    companion object {
        fun createRuntimeExceptionConvertingAdapter(terminal: Terminal): IOSafeTerminal {
            return if (terminal is ExtendedTerminal) {
                createRuntimeExceptionConvertingAdapter(terminal)
            } else {
                IOSafeTerminalAdapter(terminal, ConvertToRuntimeException())
            }
        }

        fun createRuntimeExceptionConvertingAdapter(terminal: ExtendedTerminal): IOSafeExtendedTerminal {
            return Extended(terminal, ConvertToRuntimeException())
        }

        fun createDoNothingOnExceptionAdapter(terminal: Terminal): IOSafeTerminal {
            return if (terminal is ExtendedTerminal) {
                createDoNothingOnExceptionAdapter(terminal)
            } else {
                IOSafeTerminalAdapter(terminal, DoNothingAndOrReturnNull())
            }
        }

        fun createDoNothingOnExceptionAdapter(terminal: ExtendedTerminal): IOSafeExtendedTerminal {
            return Extended(terminal, DoNothingAndOrReturnNull())
        }
    }
}
