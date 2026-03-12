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

/**
 * Simple implementation of TextImage that keeps the content as a two-dimensional TextCharacter array. Copy operations
 * between two BasicTextImage classes are semi-optimized by using com.googlecode.lanterna.internal.compat.System.arraycopy instead of iterating over each
 * character and copying them over one by one.
 * @author martin
 */
class BasicTextImage private constructor(
    override val size: TerminalSize,
    toCopy: Array<Array<TextCharacter>>,
    initialContent: TextCharacter,
) : TextImage {
    private val buffer: Array<Array<TextCharacter>>

    constructor(columns: Int, rows: Int) : this(TerminalSize(columns, rows))

    constructor(size: TerminalSize?) : this(
        requireNotNull(size) { "Cannot create BasicTextImage with null size" },
        TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT),
    )

    constructor(size: TerminalSize?, initialContent: TextCharacter?) : this(
        requireNotNull(size) { "Cannot create BasicTextImage with null size" },
        emptyArray(),
        requireNotNull(initialContent) { "Cannot create BasicTextImage with null filler" },
    )

    init {
        val rows = size.rows
        val columns = size.columns
        buffer =
            Array(rows) { y ->
                Array(columns) { x ->
                    if (y < toCopy.size && x < toCopy[y].size) {
                        toCopy[y][x]
                    } else {
                        initialContent
                    }
                }
            }
    }

    override fun setAll(character: TextCharacter?) {
        val fillCharacter =
            requireNotNull(character) {
                "Cannot call BasicTextImage.setAll(..) with null character"
            }
        for (line in buffer) {
            line.fill(fillCharacter)
        }
    }

    override fun resize(
        newSize: TerminalSize?,
        filler: TextCharacter?,
    ): BasicTextImage {
        val targetSize =
            requireNotNull(newSize) {
                "Cannot resize BasicTextImage with null newSize"
            }
        val fillCharacter =
            requireNotNull(filler) {
                "Cannot resize BasicTextImage with null filler"
            }
        if (targetSize.rows == buffer.size &&
            (buffer.isEmpty() || targetSize.columns == buffer[0].size)
        ) {
            return this
        }
        return BasicTextImage(targetSize, buffer, fillCharacter)
    }

    override fun setCharacterAt(
        position: TerminalPosition?,
        character: TextCharacter?,
    ) {
        val p =
            requireNotNull(position) {
                "Cannot call BasicTextImage.setCharacterAt(..) with null position"
            }
        setCharacterAt(p.column, p.row, character)
    }

    override fun setCharacterAt(
        column: Int,
        row: Int,
        character: TextCharacter?,
    ) {
        val value =
            requireNotNull(character) {
                "Cannot call BasicTextImage.setCharacterAt(..) with null character"
            }
        if (column < 0 || row < 0 || row >= buffer.size || (buffer.isNotEmpty() && column >= buffer[0].size)) {
            return
        }

        if (column > 0 && buffer[row][column - 1].isDoubleWidth) {
            buffer[row][column - 1] = buffer[row][column - 1].withCharacter(' ')
        }

        buffer[row][column] = value

        if (value.isDoubleWidth && column + 1 < buffer[0].size) {
            buffer[row][column + 1] = value.withCharacter(' ')
        }
    }

    override fun getCharacterAt(position: TerminalPosition?): TextCharacter? {
        val p =
            requireNotNull(position) {
                "Cannot call BasicTextImage.getCharacterAt(..) with null position"
            }
        return getCharacterAt(p.column, p.row)
    }

    override fun getCharacterAt(
        column: Int,
        row: Int,
    ): TextCharacter? {
        if (column < 0 || row < 0 || row >= buffer.size || buffer.isEmpty() || column >= buffer[0].size) {
            return null
        }
        return buffer[row][column]
    }

    override fun copyTo(destination: TextImage?) {
        if (buffer.isNotEmpty()) {
            copyTo(destination, 0, buffer.size, 0, buffer[0].size, 0, 0)
        }
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
        val target = requireNotNull(destination) { "destination" }

        var srcStartRow = startRowIndex
        var srcRows = rows
        var srcStartColumn = startColumnIndex
        var srcColumns = columns
        var dstRowOffset = destinationRowOffset
        var dstColumnOffset = destinationColumnOffset

        if (srcStartColumn < 0) {
            dstColumnOffset += -srcStartColumn
            srcColumns += srcStartColumn
            srcStartColumn = 0
        }
        if (srcStartRow < 0) {
            dstRowOffset += -srcStartRow
            srcRows += srcStartRow
            srcStartRow = 0
        }

        if (dstColumnOffset < 0) {
            srcStartColumn -= dstColumnOffset
            srcColumns += dstColumnOffset
            dstColumnOffset = 0
        }
        if (dstRowOffset < 0) {
            srcStartRow -= dstRowOffset
            srcRows += dstRowOffset
            dstRowOffset = 0
        }

        srcRows = kotlin.math.min(buffer.size - srcStartRow, srcRows)
        srcColumns =
            if (srcRows > 0) {
                kotlin.math.min(buffer[0].size - srcStartColumn, srcColumns)
            } else {
                0
            }

        val targetSize = target.size ?: TerminalSize(0, 0)
        srcColumns = kotlin.math.min(targetSize.columns - dstColumnOffset, srcColumns)
        srcRows = kotlin.math.min(targetSize.rows - dstRowOffset, srcRows)

        if (srcColumns <= 0 || srcRows <= 0) {
            return
        }

        if (target is BasicTextImage) {
            var targetRow = dstRowOffset
            var y = srcStartRow
            while (y < srcStartRow + srcRows && targetRow < targetSize.rows) {
                com.googlecode.lanterna.internal.compat.System.arraycopy(
                    buffer[y],
                    srcStartColumn,
                    target.buffer[targetRow++],
                    dstColumnOffset,
                    srcColumns,
                )
                y++
            }
        } else {
            for (y in srcStartRow until srcStartRow + srcRows) {
                var x = srcStartColumn
                while (x < srcStartColumn + srcColumns) {
                    var character = buffer[y][x]
                    if (character.isDoubleWidth) {
                        if (x + 1 < srcStartColumn + srcColumns) {
                            target.setCharacterAt(
                                x - srcStartColumn + dstColumnOffset,
                                y - srcStartRow + dstRowOffset,
                                character.withCharacter(' '),
                            )
                        } else if (x + 1 == srcStartColumn + srcColumns) {
                            character = character.withCharacter(' ')
                        }
                    }
                    target.setCharacterAt(
                        x - srcStartColumn + dstColumnOffset,
                        y - srcStartRow + dstRowOffset,
                        character,
                    )
                    if (character.isDoubleWidth) {
                        x++
                    }
                    x++
                }
            }
        }

        if (dstColumnOffset > 0) {
            val destinationX = dstColumnOffset - 1
            for (y in srcStartRow until srcStartRow + srcRows) {
                val destinationY = y - srcStartRow + dstRowOffset
                val neighbour = target.getCharacterAt(destinationX, destinationY)
                if (neighbour != null && neighbour.isDoubleWidth) {
                    target.setCharacterAt(destinationX, destinationY, neighbour.withCharacter(' '))
                }
            }
        }
    }

    override fun newTextGraphics(): TextGraphics {
        return object : AbstractTextGraphics() {
            override fun setCharacter(
                columnIndex: Int,
                rowIndex: Int,
                textCharacter: TextCharacter?,
            ): TextGraphics {
                this@BasicTextImage.setCharacterAt(columnIndex, rowIndex, textCharacter)
                return this
            }

            override fun getCharacter(
                column: Int,
                row: Int,
            ): TextCharacter? {
                return this@BasicTextImage.getCharacterAt(column, row)
            }

            override val size: TerminalSize
                get() = this@BasicTextImage.size
        }
    }

    private fun newBlankLine(): Array<TextCharacter> {
        val line = Array(size.columns) { TextCharacter.DEFAULT_CHARACTER }
        line.fill(TextCharacter.DEFAULT_CHARACTER)
        return line
    }

    override fun scrollLines(
        firstLine: Int,
        lastLine: Int,
        distance: Int,
    ) {
        var start = firstLine
        var end = lastLine
        var delta = distance

        if (start < 0) {
            start = 0
        }
        if (end >= size.rows) {
            end = size.rows - 1
        }

        if (start < end) {
            if (delta > 0) {
                var curLine = start
                while (curLine <= end - delta) {
                    buffer[curLine] = buffer[curLine + delta]
                    curLine++
                }
                while (curLine <= end) {
                    buffer[curLine] = newBlankLine()
                    curLine++
                }
            } else if (delta < 0) {
                var curLine = end
                delta = -delta
                while (curLine >= start + delta) {
                    buffer[curLine] = buffer[curLine - delta]
                    curLine--
                }
                while (curLine >= start) {
                    buffer[curLine] = newBlankLine()
                    curLine--
                }
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder(size.rows * (size.columns + 1) + 50)
        sb.append('{').append(size.columns).append('x').append(size.rows).append('}').append('\n')
        for (line in buffer) {
            for (tc in line) {
                sb.append(tc.characterString)
            }
            sb.append('\n')
        }
        return sb.toString()
    }
}
