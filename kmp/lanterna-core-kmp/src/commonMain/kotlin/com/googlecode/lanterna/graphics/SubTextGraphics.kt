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
 * This implementation of TextGraphics will take a proper object and composite a view on top of it.
 */
internal class SubTextGraphics(
    private val underlyingTextGraphics: TextGraphics,
    private val topLeft: TerminalPosition,
    screenRelative: TerminalPosition,
    override val size: TerminalSize,
) : AbstractTextGraphics() {
    override val screenLocation: TerminalPosition = requireNotNull(screenRelative.plus(topLeft))

    private fun project(
        column: Int,
        row: Int,
    ): TerminalPosition {
        return requireNotNull(topLeft.withRelative(column, row))
    }

    override fun setCharacter(
        columnIndex: Int,
        rowIndex: Int,
        textCharacter: TextCharacter?,
    ): TextGraphics {
        val writableArea = size
        if (columnIndex < 0 || columnIndex >= writableArea.columns ||
            rowIndex < 0 || rowIndex >= writableArea.rows
        ) {
            return this
        }
        val projectedPosition = project(columnIndex, rowIndex)
        underlyingTextGraphics.setCharacter(projectedPosition, textCharacter)
        return this
    }

    override fun getCharacter(
        column: Int,
        row: Int,
    ): TextCharacter? {
        val projectedPosition = project(column, row)
        return underlyingTextGraphics.getCharacter(projectedPosition.column, projectedPosition.row)
    }
}
