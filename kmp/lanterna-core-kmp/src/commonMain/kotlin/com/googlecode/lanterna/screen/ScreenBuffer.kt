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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextImage

open class ScreenBuffer : TextImage {
    private val backend: BasicTextImage

    constructor(size: TerminalSize?, filler: TextCharacter?) : this(BasicTextImage(size, filler))

    private constructor(backend: BasicTextImage) {
        this.backend = backend
    }

    override fun resize(newSize: TerminalSize?, filler: TextCharacter?): ScreenBuffer {
        val resizedBackend = backend.resize(newSize, filler)
        return ScreenBuffer(resizedBackend)
    }

    internal open fun isVeryDifferent(other: ScreenBuffer, threshold: Int): Boolean {
        if (!size.equals(other.size)) {
            throw IllegalArgumentException(
                "Can only call isVeryDifferent comparing two ScreenBuffers of the same size!" +
                    " This is probably a bug in Lanterna."
            )
        }
        var differences = 0
        for (y in 0 until size.rows) {
            for (x in 0 until size.columns) {
                if (!getCharacterAt(x, y).equals(other.getCharacterAt(x, y))) {
                    if (++differences >= threshold) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun getSize(): TerminalSize {
        return backend.size
    }

    override fun getCharacterAt(position: TerminalPosition?): TextCharacter {
        return backend.getCharacterAt(position)
    }

    override fun getCharacterAt(column: Int, row: Int): TextCharacter {
        return backend.getCharacterAt(column, row)
    }

    override fun setCharacterAt(position: TerminalPosition?, character: TextCharacter?) {
        backend.setCharacterAt(position, character)
    }

    override fun setCharacterAt(column: Int, row: Int, character: TextCharacter?) {
        backend.setCharacterAt(column, row, character)
    }

    override fun setAll(character: TextCharacter?) {
        backend.setAll(character)
    }

    override fun newTextGraphics(): TextGraphics {
        return backend.newTextGraphics()
    }

    override fun copyTo(destination: TextImage?) {
        var actualDestination = destination
        if (actualDestination is ScreenBuffer) {
            actualDestination = actualDestination.backend
        }
        backend.copyTo(actualDestination)
    }

    override fun copyTo(
        destination: TextImage?,
        startRowIndex: Int,
        rows: Int,
        startColumnIndex: Int,
        columns: Int,
        destinationRowOffset: Int,
        destinationColumnOffset: Int
    ) {
        var actualDestination = destination
        if (actualDestination is ScreenBuffer) {
            actualDestination = actualDestination.backend
        }
        backend.copyTo(
            actualDestination,
            startRowIndex,
            rows,
            startColumnIndex,
            columns,
            destinationRowOffset,
            destinationColumnOffset
        )
    }

    open fun copyFrom(
        source: TextImage,
        startRowIndex: Int,
        rows: Int,
        startColumnIndex: Int,
        columns: Int,
        destinationRowOffset: Int,
        destinationColumnOffset: Int
    ) {
        source.copyTo(
            backend,
            startRowIndex,
            rows,
            startColumnIndex,
            columns,
            destinationRowOffset,
            destinationColumnOffset
        )
    }

    override fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        backend.scrollLines(firstLine, lastLine, distance)
    }

    override fun toString(): String {
        return backend.toString()
    }
}
