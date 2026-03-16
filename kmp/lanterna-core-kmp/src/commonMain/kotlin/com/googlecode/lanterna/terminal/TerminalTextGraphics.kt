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

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.AbstractTextGraphics
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.internal.compat.AtomicInteger
import com.googlecode.lanterna.internal.io.IOException
import kotlin.collections.HashMap

internal class TerminalTextGraphics
    @Throws(IOException::class)
    constructor(
        private val terminal: Terminal,
    ) : AbstractTextGraphics() {
        override val size: TerminalSize = terminal.terminalSize ?: TerminalSize.ZERO

        private val writeHistory: MutableMap<TerminalPosition, TextCharacter> = HashMap()
        private val manageCallStackSize = AtomicInteger(0)
        private var lastCharacter: TextCharacter? = null
        private var lastPosition: TerminalPosition? = null

        override fun setCharacter(
            columnIndex: Int,
            rowIndex: Int,
            textCharacter: TextCharacter?,
        ): TextGraphics {
            return setCharacter(TerminalPosition(columnIndex, rowIndex), textCharacter)
        }

        override fun setCharacter(
            position: TerminalPosition?,
            textCharacter: TextCharacter?,
        ): TextGraphics {
            val safePosition = position ?: return this
            val safeCharacter = textCharacter ?: return this
            try {
                if (manageCallStackSize.get() > 0) {
                    if (lastCharacter == null || lastCharacter != safeCharacter) {
                        applyGraphicState(safeCharacter)
                        lastCharacter = safeCharacter
                    }
                    if (lastPosition == null || lastPosition != safePosition) {
                        terminal.setCursorPosition(safePosition.column, safePosition.row)
                        lastPosition = safePosition
                    }
                } else {
                    terminal.setCursorPosition(safePosition.column, safePosition.row)
                    applyGraphicState(safeCharacter)
                }
                terminal.putString(safeCharacter.characterString)
                if (manageCallStackSize.get() > 0) {
                    lastPosition = safePosition.withRelativeColumn(1)
                }
                writeHistory[safePosition] = safeCharacter
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return this
        }

        override fun getCharacter(
            column: Int,
            row: Int,
        ): TextCharacter? {
            return getCharacter(TerminalPosition(column, row))
        }

        override fun getCharacter(position: TerminalPosition?): TextCharacter? {
            return if (position == null) null else writeHistory[position]
        }

        @Throws(IOException::class)
        private fun applyGraphicState(textCharacter: TextCharacter) {
            terminal.resetColorAndSGR()
            terminal.setForegroundColor(textCharacter.foregroundColor)
            terminal.setBackgroundColor(textCharacter.backgroundColor)
            for (sgr in textCharacter.getModifiers()) {
                terminal.enableSGR(sgr)
            }
        }

        override fun drawLine(
            fromPoint: TerminalPosition?,
            toPoint: TerminalPosition?,
            character: Char,
        ): TextGraphics {
            try {
                enterAtomic()
                super.drawLine(fromPoint, toPoint, character)
                return this
            } finally {
                leaveAtomic()
            }
        }

        override fun drawTriangle(
            p1: TerminalPosition?,
            p2: TerminalPosition?,
            p3: TerminalPosition?,
            character: Char,
        ): TextGraphics {
            try {
                enterAtomic()
                super.drawTriangle(p1, p2, p3, character)
                return this
            } finally {
                leaveAtomic()
            }
        }

        override fun fillTriangle(
            p1: TerminalPosition?,
            p2: TerminalPosition?,
            p3: TerminalPosition?,
            character: Char,
        ): TextGraphics {
            try {
                enterAtomic()
                super.fillTriangle(p1, p2, p3, character)
                return this
            } finally {
                leaveAtomic()
            }
        }

        override fun fillRectangle(
            topLeft: TerminalPosition?,
            size: TerminalSize?,
            character: Char,
        ): TextGraphics {
            try {
                enterAtomic()
                super.fillRectangle(topLeft, size, character)
                return this
            } finally {
                leaveAtomic()
            }
        }

        override fun drawRectangle(
            topLeft: TerminalPosition?,
            size: TerminalSize?,
            character: Char,
        ): TextGraphics {
            try {
                enterAtomic()
                super.drawRectangle(topLeft, size, character)
                return this
            } finally {
                leaveAtomic()
            }
        }

        override fun putString(
            column: Int,
            row: Int,
            string: String?,
        ): TextGraphics {
            try {
                enterAtomic()
                return super.putString(column, row, string) ?: this
            } finally {
                leaveAtomic()
            }
        }

        private fun enterAtomic() {
            manageCallStackSize.incrementAndGet()
        }

        private fun leaveAtomic() {
            if (manageCallStackSize.decrementAndGet() == 0) {
                lastPosition = null
                lastCharacter = null
            }
        }
    }
