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
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.internal.compat.TimeUnit

interface IOSafeTerminal : Terminal {
    override var cursorPosition: TerminalPosition?
    override val terminalSize: TerminalSize?

    override fun enterPrivateMode()
    override fun exitPrivateMode()
    override fun clearScreen()
    override fun setCursorPosition(x: Int, y: Int)
    override fun setCursorVisible(visible: Boolean)
    override fun putCharacter(c: Char)
    override fun putString(string: String?)
    override fun enableSGR(sgr: SGR?)
    override fun disableSGR(sgr: SGR?)
    override fun resetColorAndSGR()
    override fun setForegroundColor(color: TextColor?)
    override fun setBackgroundColor(color: TextColor?)
    override fun enquireTerminal(timeout: Int, timeoutUnit: TimeUnit?): ByteArray?
    override fun bell()
    override fun flush()
    override fun pollInput(): KeyStroke?
    override fun readInput(): KeyStroke?
    override fun close()
}
