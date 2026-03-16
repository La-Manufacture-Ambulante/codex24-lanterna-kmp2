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

class ScreenBuffer private constructor(private val backend: BasicTextImage) : TextImage {
    constructor(size: TerminalSize?, filler: TextCharacter?) : this(BasicTextImage(size, filler))

    override val size: TerminalSize?
        get() = backend.size

    override fun resize(
        newSize: TerminalSize?,
        filler: TextCharacter?,
    ): ScreenBuffer {
        return ScreenBuffer(backend.resize(newSize, filler))
    }

    internal fun isVeryDifferent(
        other: ScreenBuffer,
        threshold: Int,
    ): Boolean {
        require(size == other.size) {
            "Can only call isVeryDifferent comparing two ScreenBuffers of the same size! This is probably a bug in Lanterna."
        }
        var differences = 0
        for (y in 0 until (size?.rows ?: 0)) {
            for (x in 0 until (size?.columns ?: 0)) {
                if (getCharacterAt(x, y) != other.getCharacterAt(x, y) && ++differences >= threshold) {
                    return true
                }
            }
        }
        return false
    }

    override fun getCharacterAt(position: TerminalPosition?): TextCharacter? = backend.getCharacterAt(position)

    override fun getCharacterAt(
        column: Int,
        row: Int,
    ): TextCharacter? = backend.getCharacterAt(column, row)

    override fun setCharacterAt(
        position: TerminalPosition?,
        character: TextCharacter?,
    ) {
        backend.setCharacterAt(position, character)
    }

    override fun setCharacterAt(
        column: Int,
        row: Int,
        character: TextCharacter?,
    ) {
        backend.setCharacterAt(column, row, character)
    }

    override fun setAll(character: TextCharacter?) {
        backend.setAll(character)
    }

    override fun newTextGraphics(): TextGraphics? = backend.newTextGraphics()

    override fun copyTo(destination: TextImage?) {
        val actualDestination = if (destination is ScreenBuffer) destination.backend else destination
        backend.copyTo(actualDestination)
    }

    override fun copyTo(
        destination: TextImage?,
        startRowIndex: Int,
        rows: Int,
        startColumnIndex: Int,
        columns: Int,
        destinationRowOffset: Int,
        destinationColumnOffset: Int,
    ) {
        val actualDestination = if (destination is ScreenBuffer) destination.backend else destination
        backend.copyTo(
            actualDestination,
            startRowIndex,
            rows,
            startColumnIndex,
            columns,
            destinationRowOffset,
            destinationColumnOffset,
        )
    }

    fun copyFrom(
        source: TextImage,
        startRowIndex: Int,
        rows: Int,
        startColumnIndex: Int,
        columns: Int,
        destinationRowOffset: Int,
        destinationColumnOffset: Int,
    ) {
        source.copyTo(
            backend,
            startRowIndex,
            rows,
            startColumnIndex,
            columns,
            destinationRowOffset,
            destinationColumnOffset,
        )
    }

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        backend.scrollLines(firstLine, lastLine, distance)
    }

    override fun toString(): String = backend.toString()
}
