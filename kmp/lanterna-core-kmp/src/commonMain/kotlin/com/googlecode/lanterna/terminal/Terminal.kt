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
import com.googlecode.lanterna.input.InputProvider
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.TimeUnit

interface Terminal : InputProvider, Closeable {
    @Throws(IOException::class)
    fun enterPrivateMode()

    @Throws(IOException::class)
    fun exitPrivateMode()

    @Throws(IOException::class)
    fun clearScreen()

    @Throws(IOException::class)
    fun setCursorPosition(
        x: Int,
        y: Int,
    )

    @get:Throws(IOException::class)
    @set:Throws(IOException::class)
    var cursorPosition: TerminalPosition?

    @Throws(IOException::class)
    fun setCursorVisible(visible: Boolean)

    @Throws(IOException::class)
    fun putCharacter(c: Char)

    @Throws(IOException::class)
    fun putString(string: String?)

    @Throws(IOException::class)
    fun newTextGraphics(): TextGraphics?

    @Throws(IOException::class)
    fun enableSGR(sgr: SGR?)

    @Throws(IOException::class)
    fun disableSGR(sgr: SGR?)

    @Throws(IOException::class)
    fun resetColorAndSGR()

    @Throws(IOException::class)
    fun setForegroundColor(color: TextColor?)

    @Throws(IOException::class)
    fun setBackgroundColor(color: TextColor?)

    fun addResizeListener(listener: TerminalResizeListener?)

    fun removeResizeListener(listener: TerminalResizeListener?)

    @get:Throws(IOException::class)
    val terminalSize: TerminalSize?

    @Throws(IOException::class)
    fun enquireTerminal(
        timeout: Int,
        timeoutUnit: TimeUnit?,
    ): ByteArray?

    @Throws(IOException::class)
    fun bell()

    @Throws(IOException::class)
    fun flush()

    @Throws(IOException::class)
    override fun close()
}
