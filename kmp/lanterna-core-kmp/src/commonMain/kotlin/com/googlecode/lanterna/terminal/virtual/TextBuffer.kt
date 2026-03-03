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
import java.util.ArrayList
import java.util.LinkedList
import java.util.ListIterator

/**
 * This class is used to store lines of text inside of a terminal emulator. As used by [DefaultVirtualTerminal], it keeps
 * two [TextBuffer]s, one for private mode and one for normal mode and it can switch between them as needed.
 */
internal class TextBuffer {
    private val lines: LinkedList<MutableList<TextCharacter>>

    init {
        lines = LinkedList()
        newLine()
    }

    @Synchronized
    internal fun newLine() {
        lines.add(ArrayList(200))
    }

    @Synchronized
    internal fun removeTopLines(numberOfLinesToRemove: Int) {
        for (i in 0 until numberOfLinesToRemove) {
            lines.removeFirst()
        }
    }

    @Synchronized
    internal fun clear() {
        lines.clear()
        newLine()
    }

    internal fun getLinesFrom(rowNumber: Int): ListIterator<MutableList<TextCharacter>> {
        return lines.listIterator(rowNumber)
    }

    @Synchronized
    internal fun getLineCount(): Int {
        return lines.size
    }

    @Synchronized
    internal fun setCharacter(lineNumber: Int, columnIndex: Int, textCharacter: TextCharacter?): Int {
        if (lineNumber < 0 || columnIndex < 0) {
            throw IllegalArgumentException(
                "Illegal argument to TextBuffer.setCharacter(..), lineNumber = " +
                    lineNumber + ", columnIndex = " + columnIndex
            )
        }
        val resolvedTextCharacter = textCharacter ?: TextCharacter.DEFAULT_CHARACTER
        while (lineNumber >= lines.size) {
            newLine()
        }
        val line = lines[lineNumber]
        while (line.size <= columnIndex) {
            line.add(TextCharacter.DEFAULT_CHARACTER)
        }

        // Default
        var returnStyle = 0

        // Check if we are overwriting a double-width character, in that case we need to reset the other half
        if (line[columnIndex].isDoubleWidth) {
            line[columnIndex + 1] = line[columnIndex].withCharacter(' ')
            returnStyle = 1 // this character and the one to the right
        } else if (line[columnIndex] === DOUBLE_WIDTH_CHAR_PADDING) {
            line[columnIndex - 1] = TextCharacter.DEFAULT_CHARACTER
            returnStyle = 2 // this character and the one to the left
        }
        line[columnIndex] = resolvedTextCharacter

        if (resolvedTextCharacter.isDoubleWidth) {
            // We don't report this column as dirty (yet), it's implied since a double-width character is reported
            setCharacter(lineNumber, columnIndex + 1, DOUBLE_WIDTH_CHAR_PADDING)
        }
        return returnStyle
    }

    @Synchronized
    internal fun getCharacter(lineNumber: Int, columnIndex: Int): TextCharacter {
        if (lineNumber < 0 || columnIndex < 0) {
            throw IllegalArgumentException(
                "Illegal argument to TextBuffer.getCharacter(..), lineNumber = " +
                    lineNumber + ", columnIndex = " + columnIndex
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
        if (textCharacter === DOUBLE_WIDTH_CHAR_PADDING) {
            return line[columnIndex - 1]
        }
        return textCharacter
    }

    override fun toString(): String {
        val bo = StringBuilder()
        for (line in lines) {
            val b = StringBuilder()
            for (c in line) {
                b.append(c.characterString)
            }
            bo.append(b.toString().replaceFirst("\\s+$".toRegex(), ""))
            bo.append('\n')
        }
        return bo.toString()
    }

    private companion object {
        val DOUBLE_WIDTH_CHAR_PADDING: TextCharacter = TextCharacter(' ')
    }
}
