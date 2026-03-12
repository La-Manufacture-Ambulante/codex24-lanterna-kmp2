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

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.internal.compat.LinkedList
import kotlin.collections.ArrayList

/**
 * This class is used to store lines of text inside of a terminal emulator. As used by [DefaultVirtualTerminal], it keeps
 * two [TextBuffer]s, one for private mode and one for normal mode and it can switch between them as needed.
 */
internal class TextBuffer {
    private val lines: LinkedList<MutableList<TextCharacter>> = LinkedList()

    val lineCount: Int
        get() = lines.size

    init {
        newLine()
    }

    fun newLine() {
        lines.add(ArrayList(200))
    }

    fun removeTopLines(numberOfLinesToRemove: Int) {
        repeat(numberOfLinesToRemove) {
            lines.removeFirst()
        }
    }

    fun clear() {
        lines.clear()
        newLine()
    }

    fun getLinesFrom(rowNumber: Int): MutableListIterator<MutableList<TextCharacter>> = lines.listIterator(rowNumber)

    fun setCharacter(
        lineNumber: Int,
        columnIndex: Int,
        textCharacter: TextCharacter?,
    ): Int {
        if (lineNumber < 0 || columnIndex < 0) {
            throw IllegalArgumentException(
                "Illegal argument to TextBuffer.setCharacter(..), lineNumber = $lineNumber, columnIndex = $columnIndex",
            )
        }
        val resolvedCharacter = textCharacter ?: TextCharacter.DEFAULT_CHARACTER
        while (lineNumber >= lines.size) {
            newLine()
        }
        val line = lines[lineNumber]
        while (line.size <= columnIndex) {
            line.add(TextCharacter.DEFAULT_CHARACTER)
        }

        var returnStyle = 0
        if (line[columnIndex].isDoubleWidth) {
            line[columnIndex + 1] = line[columnIndex].withCharacter(' ')
            returnStyle = 1
        } else if (line[columnIndex] === DOUBLE_WIDTH_CHAR_PADDING) {
            line[columnIndex - 1] = TextCharacter.DEFAULT_CHARACTER
            returnStyle = 2
        }
        line[columnIndex] = resolvedCharacter

        if (resolvedCharacter.isDoubleWidth) {
            setCharacter(lineNumber, columnIndex + 1, DOUBLE_WIDTH_CHAR_PADDING)
        }
        return returnStyle
    }

    fun getCharacter(
        lineNumber: Int,
        columnIndex: Int,
    ): TextCharacter {
        if (lineNumber < 0 || columnIndex < 0) {
            throw IllegalArgumentException(
                "Illegal argument to TextBuffer.getCharacter(..), lineNumber = $lineNumber, columnIndex = $columnIndex",
            )
        }
        if (lineNumber >= lines.size) {
            return TextCharacter.DEFAULT_CHARACTER
        }
        val line = lines[lineNumber]
        if (line.size <= columnIndex) {
            return TextCharacter.DEFAULT_CHARACTER
        }
        val textCharacter = line[columnIndex]
        return if (textCharacter === DOUBLE_WIDTH_CHAR_PADDING) line[columnIndex - 1] else textCharacter
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (line in lines) {
            val lineBuilder = StringBuilder()
            for (character in line) {
                lineBuilder.append(character.characterString)
            }
            builder.append(lineBuilder.toString().replaceFirst("\\s+$".toRegex(), ""))
            builder.append('\n')
        }
        return builder.toString()
    }

    companion object {
        private val DOUBLE_WIDTH_CHAR_PADDING = TextCharacter(' ')
    }
}
