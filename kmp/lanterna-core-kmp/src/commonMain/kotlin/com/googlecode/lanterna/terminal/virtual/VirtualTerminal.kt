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
package com.googlecode.lanterna.terminal.virtual

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.IOSafeTerminal

interface VirtualTerminal : IOSafeTerminal {
    fun setTerminalSize(newSize: TerminalSize?)

    fun addVirtualTerminalListener(listener: VirtualTerminalListener?)

    fun removeVirtualTerminalListener(listener: VirtualTerminalListener?)

    fun setBacklogSize(backlogSize: Int)

    fun isCursorVisible(): Boolean

    fun addInput(keyStroke: KeyStroke?)

    fun getCursorBufferPosition(): TerminalPosition?

    fun getBufferCharacter(position: TerminalPosition?): TextCharacter?

    fun getBufferCharacter(column: Int, row: Int): TextCharacter?

    fun getCharacter(position: TerminalPosition?): TextCharacter?

    fun getCharacter(column: Int, row: Int): TextCharacter?

    fun getBufferLineCount(): Int

    fun forEachLine(startRow: Int, endRow: Int, bufferWalker: BufferWalker?)

    interface BufferLine {
        fun getCharacterAt(column: Int): TextCharacter?
    }

    interface BufferWalker {
        fun onLine(rowNumber: Int, bufferLine: BufferLine?)
    }
}
