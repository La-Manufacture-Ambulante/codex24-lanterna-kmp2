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
package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter

/**
 * Wraps another TextGraphics and prints each character twice horizontally.
 */
class DoublePrintingTextGraphics(private val underlyingTextGraphics: TextGraphics) : AbstractTextGraphics() {
    override fun setCharacter(
        columnIndex: Int,
        rowIndex: Int,
        textCharacter: TextCharacter?,
    ): TextGraphics {
        val adjustedColumn = columnIndex * 2
        underlyingTextGraphics.setCharacter(adjustedColumn, rowIndex, textCharacter)
        underlyingTextGraphics.setCharacter(adjustedColumn + 1, rowIndex, textCharacter)
        return this
    }

    override fun getCharacter(
        columnIndex: Int,
        rowIndex: Int,
    ): TextCharacter? {
        val adjustedColumn = columnIndex * 2
        return underlyingTextGraphics.getCharacter(adjustedColumn, rowIndex)
    }

    override val size: TerminalSize
        get() {
            val innerSize = underlyingTextGraphics.size ?: TerminalSize(0, 0)
            return requireNotNull(innerSize.withColumns(innerSize.columns / 2))
        }

    override fun toScreenPosition(pos: TerminalPosition?): TerminalPosition? {
        val position = pos ?: TerminalPosition.TOP_LEFT_CORNER
        return underlyingTextGraphics.toScreenPosition(position.multiply(MULTIPLIER))
    }

    companion object {
        private val MULTIPLIER = TerminalPosition(2, 1)
    }
}
