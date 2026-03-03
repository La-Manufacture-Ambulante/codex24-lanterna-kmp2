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
import com.googlecode.lanterna.TextColor
import java.util.Arrays

open class BasicTextImage : TextImage {
    private val size: TerminalSize
    private val buffer: Array<Array<TextCharacter>>

    constructor(columns: Int, rows: Int) : this(TerminalSize(columns, rows))

    constructor(size: TerminalSize?) : this(
        size,
        TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT)
    )

    constructor(size: TerminalSize?, initialContent: TextCharacter?) : this(
        size,
        emptyArray<Array<TextCharacter>>(),
        initialContent
    )

    private constructor(
        size: TerminalSize?,
        toCopy: Array<Array<TextCharacter>>?,
        initialContent: TextCharacter?
    ) {
        if (size == null || toCopy == null || initialContent == null) {
            throw IllegalArgumentException(
                "Cannot create BasicTextImage with null " +
                    (if (size == null) "size" else if (toCopy == null) "toCopy" else "filler")
            )
        }
        this.size = size

        val rows = size.getRows()
        val columns = size.getColumns()
        buffer = Array(rows) { Array(columns) { initialContent } }
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (y < toCopy.size && x < toCopy[y].size) {
                    buffer[y][x] = toCopy[y][x]
                } else {
                    buffer[y][x] = initialContent
                }
            }
        }
    }

    override open fun getSize(): TerminalSize {
        return size
    }

    override open fun setAll(character: TextCharacter?) {
        if (character == null) {
            throw IllegalArgumentException("Cannot call BasicTextImage.setAll(..) with null character")
        }
        for (line in buffer) {
            Arrays.fill(line, character)
        }
    }

    override open fun resize(newSize: TerminalSize?, filler: TextCharacter?): BasicTextImage {
        if (newSize == null || filler == null) {
            throw IllegalArgumentException(
                "Cannot resize BasicTextImage with null " +
                    (if (newSize == null) "newSize" else "filler")
            )
        }
        if (newSize.getRows() == buffer.size &&
            (buffer.isEmpty() || newSize.getColumns() == buffer[0].size)
        ) {
            return this
        }
        return BasicTextImage(newSize, buffer, filler)
    }

    override open fun setCharacterAt(position: TerminalPosition?, character: TextCharacter?) {
        if (position == null) {
            throw IllegalArgumentException("Cannot call BasicTextImage.setCharacterAt(..) with null position")
        }
        setCharacterAt(position.getColumn(), position.getRow(), character)
    }

    override open fun setCharacterAt(column: Int, row: Int, character: TextCharacter?) {
        if (character == null) {
            throw IllegalArgumentException("Cannot call BasicTextImage.setCharacterAt(..) with null character")
        }
        if (column < 0 || row < 0 || row >= buffer.size || column >= buffer[0].size) {
            return
        }

        if (column > 0 && buffer[row][column - 1].isDoubleWidth()) {
            buffer[row][column - 1] = buffer[row][column - 1].withCharacter(' ')
        }

        buffer[row][column] = character

        if (character.isDoubleWidth() && column + 1 < buffer[0].size) {
            buffer[row][column + 1] = character.withCharacter(' ')
        }
    }

    override open fun getCharacterAt(position: TerminalPosition?): TextCharacter? {
        if (position == null) {
            throw IllegalArgumentException("Cannot call BasicTextImage.getCharacterAt(..) with null position")
        }
        return getCharacterAt(position.getColumn(), position.getRow())
    }

    override open fun getCharacterAt(column: Int, row: Int): TextCharacter? {
        if (column < 0 || row < 0 || row >= buffer.size || column >= buffer[0].size) {
            return null
        }
        return buffer[row][column]
    }

    override open fun copyTo(destination: TextImage) {
        if (buffer.isNotEmpty()) {
            copyTo(destination, 0, buffer.size, 0, buffer[0].size, 0, 0)
        }
    }

    override open fun copyTo(
        destination: TextImage,
        startRowIndex: Int,
        rows: Int,
        startColumnIndex: Int,
        columns: Int,
        destinationRowOffset: Int,
        destinationColumnOffset: Int
    ) {
        var startRowIndexVar = startRowIndex
        var rowsVar = rows
        var startColumnIndexVar = startColumnIndex
        var columnsVar = columns
        var destinationRowOffsetVar = destinationRowOffset
        var destinationColumnOffsetVar = destinationColumnOffset

        if (startColumnIndexVar < 0) {
            destinationColumnOffsetVar += -startColumnIndexVar
            columnsVar += startColumnIndexVar
            startColumnIndexVar = 0
        }
        if (startRowIndexVar < 0) {
            destinationRowOffsetVar += -startRowIndexVar
            rowsVar += startRowIndexVar
            startRowIndexVar = 0
        }

        if (destinationColumnOffsetVar < 0) {
            startColumnIndexVar -= destinationColumnOffsetVar
            columnsVar += destinationColumnOffsetVar
            destinationColumnOffsetVar = 0
        }
        if (destinationRowOffsetVar < 0) {
            startRowIndexVar -= destinationRowOffsetVar
            rowsVar += destinationRowOffsetVar
            destinationRowOffsetVar = 0
        }

        rowsVar = Math.min(buffer.size - startRowIndexVar, rowsVar)
        columnsVar = if (rowsVar > 0) Math.min(buffer[0].size - startColumnIndexVar, columnsVar) else 0

        columnsVar = Math.min(destination.getSize().getColumns() - destinationColumnOffsetVar, columnsVar)
        rowsVar = Math.min(destination.getSize().getRows() - destinationRowOffsetVar, rowsVar)

        if (columnsVar <= 0 || rowsVar <= 0) {
            return
        }

        val destinationSize = destination.getSize()
        if (destination is BasicTextImage) {
            var targetRow = destinationRowOffsetVar
            var y = startRowIndexVar
            while (y < startRowIndexVar + rowsVar && targetRow < destinationSize.getRows()) {
                System.arraycopy(
                    buffer[y],
                    startColumnIndexVar,
                    destination.buffer[targetRow++],
                    destinationColumnOffsetVar,
                    columnsVar
                )
                y++
            }
        } else {
            for (y in startRowIndexVar until (startRowIndexVar + rowsVar)) {
                var x = startColumnIndexVar
                while (x < startColumnIndexVar + columnsVar) {
                    var character = buffer[y][x]
                    if (character.isDoubleWidth()) {
                        if (x + 1 < startColumnIndexVar + columnsVar) {
                            destination.setCharacterAt(
                                x - startColumnIndexVar + destinationColumnOffsetVar,
                                y - startRowIndexVar + destinationRowOffsetVar,
                                character.withCharacter(' ')
                            )
                        } else if (x + 1 == startColumnIndexVar + columnsVar) {
                            character = character.withCharacter(' ')
                        }
                    }
                    destination.setCharacterAt(
                        x - startColumnIndexVar + destinationColumnOffsetVar,
                        y - startRowIndexVar + destinationRowOffsetVar,
                        character
                    )
                    if (character.isDoubleWidth()) {
                        x++
                    }
                    x++
                }
            }
        }

        if (destinationColumnOffsetVar > 0) {
            val destinationX = destinationColumnOffsetVar - 1
            for (y in startRowIndexVar until (startRowIndexVar + rowsVar)) {
                val destinationY = y - startRowIndexVar + destinationRowOffsetVar
                val neighbour = destination.getCharacterAt(destinationX, destinationY)
                if (neighbour.isDoubleWidth()) {
                    destination.setCharacterAt(destinationX, destinationY, neighbour.withCharacter(' '))
                }
            }
        }
    }

    override open fun newTextGraphics(): TextGraphics {
        return object : AbstractTextGraphics() {
            override fun setCharacter(
                columnIndex: Int,
                rowIndex: Int,
                textCharacter: TextCharacter?
            ): TextGraphics {
                this@BasicTextImage.setCharacterAt(columnIndex, rowIndex, textCharacter)
                return this
            }

            override fun getCharacter(column: Int, row: Int): TextCharacter? {
                return this@BasicTextImage.getCharacterAt(column, row)
            }

            override fun getSize(): TerminalSize {
                return size
            }
        }
    }

    private fun newBlankLine(): Array<TextCharacter> {
        val line = Array(size.getColumns()) { TextCharacter.DEFAULT_CHARACTER }
        Arrays.fill(line, TextCharacter.DEFAULT_CHARACTER)
        return line
    }

    override open fun scrollLines(firstLine: Int, lastLine: Int, distance: Int) {
        var firstLineVar = firstLine
        var lastLineVar = lastLine
        var distanceVar = distance

        if (firstLineVar < 0) {
            firstLineVar = 0
        }
        if (lastLineVar >= size.getRows()) {
            lastLineVar = size.getRows() - 1
        }
        if (firstLineVar < lastLineVar) {
            if (distanceVar > 0) {
                var curLine = firstLineVar
                while (curLine <= lastLineVar - distanceVar) {
                    buffer[curLine] = buffer[curLine + distanceVar]
                    curLine++
                }
                while (curLine <= lastLineVar) {
                    buffer[curLine] = newBlankLine()
                    curLine++
                }
            } else if (distanceVar < 0) {
                var curLine = lastLineVar
                distanceVar = -distanceVar
                while (curLine >= firstLineVar + distanceVar) {
                    buffer[curLine] = buffer[curLine - distanceVar]
                    curLine--
                }
                while (curLine >= firstLineVar) {
                    buffer[curLine] = newBlankLine()
                    curLine--
                }
            }
        }
    }

    override open fun toString(): String {
        val sb = StringBuilder(size.getRows() * (size.getColumns() + 1) + 50)
        sb.append('{').append(size.getColumns()).append('x').append(size.getRows()).append('}').append('\n')
        for (line in buffer) {
            for (tc in line) {
                sb.append(tc.getCharacterString())
            }
            sb.append('\n')
        }
        return sb.toString()
    }
}
