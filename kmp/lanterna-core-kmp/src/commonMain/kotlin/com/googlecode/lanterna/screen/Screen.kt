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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.Scrollable
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.InputProvider
import com.googlecode.lanterna.internal.compat.Closeable
import com.googlecode.lanterna.internal.io.IOException

interface Screen : InputProvider, Scrollable, Closeable {
    var cursorPosition: TerminalPosition?
    var tabBehaviour: TabBehaviour?
    val terminalSize: TerminalSize?

    @Throws(IOException::class)
    fun startScreen()

    @Throws(IOException::class)
    override fun close()

    @Throws(IOException::class)
    fun stopScreen()

    fun clear()

    fun setCharacter(column: Int, row: Int, screenCharacter: TextCharacter?)

    fun setCharacter(position: TerminalPosition?, screenCharacter: TextCharacter?)

    fun newTextGraphics(): TextGraphics?

    fun getFrontCharacter(column: Int, row: Int): TextCharacter?

    fun getFrontCharacter(position: TerminalPosition?): TextCharacter?

    fun getBackCharacter(column: Int, row: Int): TextCharacter?

    fun getBackCharacter(position: TerminalPosition?): TextCharacter?

    @Throws(IOException::class)
    fun refresh()

    @Throws(IOException::class)
    fun refresh(refreshType: RefreshType?)

    fun doResizeIfNecessary(): TerminalSize?

    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int)

    enum class RefreshType {
        AUTOMATIC,
        DELTA,
        COMPLETE,
    }

    companion object {
        val DEFAULT_CHARACTER = TextCharacter(' ')
    }
}
