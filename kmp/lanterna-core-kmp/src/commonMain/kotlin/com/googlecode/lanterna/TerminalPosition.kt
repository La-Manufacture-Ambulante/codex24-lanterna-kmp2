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
package com.googlecode.lanterna

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A 2-d position in 'terminal space'. Please note that the coordinates are 0-indexed, meaning 0x0 is the top left
 * corner of the terminal. This object is immutable so you cannot change it after it has been created. Instead, you
 * can easily create modified 'clones' by using the 'with' methods.
 *
 * @author Martin
 */
class TerminalPosition/**
 * Creates a new TerminalPosition object, which represents a location on the screen. There is no check to verify
 * that the position you specified is within the size of the current terminal and you can specify negative positions
 * as well.
 *
 * @param column Column of the location, or the "x" coordinate, zero indexed (the first column is 0)
 * @param row Row of the location, or the "y" coordinate, zero indexed (the first row is 0)
 */(
    /**
     * Returns the index of the column this position is representing, zero indexed (the first column has index 0).
     * @return Index of the column this position has
     */
    val column: Int,
    /**
     * Returns the index of the row this position is representing, zero indexed (the first row has index 0)
     * @return Index of the row this position has
     */
    val row: Int,
) : Comparable<TerminalPosition> {
    /**
     * Creates a new TerminalPosition object representing a position with the same column index as this but with a
     * supplied row index.
     * @param row Index of the row for the new position
     * @return A TerminalPosition object with the same column as this but with a specified row index
     */
    fun withRow(row: Int): TerminalPosition? {
        if (row == 0 && this.column == 0) {
            return TOP_LEFT_CORNER
        }
        return TerminalPosition(this.column, row)
    }

/**
     * Creates a new TerminalPosition object representing a position with the same row index as this but with a
     * supplied column index.
     * @param column Index of the column for the new position
     * @return A TerminalPosition object with the same row as this but with a specified column index
     */
    fun withColumn(column: Int): TerminalPosition? {
        if (column == 0 && this.row == 0) {
            return TOP_LEFT_CORNER
        }
        return TerminalPosition(column, this.row)
    }

/**
     * Creates a new TerminalPosition object representing a position on the same row, but with a column offset by a
     * supplied value. Calling this method with delta 0 will return this, calling it with a positive delta will return
     * a terminal position *delta* number of columns to the right and for negative numbers the same to the left.
     * @param delta Column offset
     * @return New terminal position based off this one but with an applied offset
     */
    fun withRelativeColumn(delta: Int): TerminalPosition? {
        if (delta == 0) {
            return this
        }
        return withColumn(column + delta)
    }

/**
     * Creates a new TerminalPosition object representing a position on the same column, but with a row offset by a
     * supplied value. Calling this method with delta 0 will return this, calling it with a positive delta will return
     * a terminal position *delta* number of rows to the down and for negative numbers the same up.
     * @param delta Row offset
     * @return New terminal position based off this one but with an applied offset
     */
    fun withRelativeRow(delta: Int): TerminalPosition? {
        if (delta == 0) {
            return this
        }
        return withRow(row + delta)
    }

/**
     * Creates a new TerminalPosition object that is 'translated' by an amount of rows and columns specified by another
     * TerminalPosition. Same as calling
     * `withRelativeRow(translate.getRow()).withRelativeColumn(translate.getColumn())`
     * @param translate How many columns and rows to translate
     * @return New TerminalPosition that is the result of the original with added translation
     */
    fun withRelative(translate: TerminalPosition): TerminalPosition? {
        return withRelative(translate.column, translate.row)
    }

/**
     * Creates a new TerminalPosition object that is 'translated' by an amount of rows and columns specified by the two
     * parameters. Same as calling
     * `withRelativeRow(deltaRow).withRelativeColumn(deltaColumn)`
     * @param deltaColumn How many columns to move from the current position in the new TerminalPosition
     * @param deltaRow How many rows to move from the current position in the new TerminalPosition
     * @return New TerminalPosition that is the result of the original position with added translation
     */
    fun withRelative(
        deltaColumn: Int,
        deltaRow: Int,
    ): TerminalPosition? {
        return withRelativeRow(deltaRow)!!.withRelativeColumn(deltaColumn)
    }

/**
     * Returns itself if it is equal to the supplied position, otherwise the supplied position. You can use this if you
     * have a position field which is frequently recalculated but often resolves to the same; it will keep the same
     * object in memory instead of swapping it out every cycle.
     * @param position Position you want to return
     * @return Itself if this position equals the position passed in, otherwise the position passed in
     */
    fun with(position: TerminalPosition?): TerminalPosition? {
        if (equals(position)) {
            return this
        }
        return position
    }

    fun plus(position: TerminalPosition?): TerminalPosition? {
        return withRelative(position!!)
    }

    fun minus(position: TerminalPosition): TerminalPosition? {
        return withRelative(-position.column, -position.row)
    }

    fun multiply(position: TerminalPosition): TerminalPosition {
        return TerminalPosition(column * position.column, row * position.row)
    }

    fun divide(denominator: TerminalPosition): TerminalPosition {
        return TerminalPosition(column / denominator.column, row / denominator.row)
    }

    fun abs(): TerminalPosition {
        val x = abs(column)
        val y = abs(row)
        return TerminalPosition(x, y)
    }

    fun min(position: TerminalPosition): TerminalPosition {
        val x = min(column, position.column)
        val y = min(row, position.row)
        return TerminalPosition(x, y)
    }

    fun max(position: TerminalPosition): TerminalPosition {
        val x = max(column, position.column)
        val y = max(row, position.row)
        return TerminalPosition(x, y)
    }

    override operator fun compareTo(o: TerminalPosition): Int {
        if (row < o.row) {
            return -1
        } else if (row == o.row) {
            if (column < o.column) {
                return -1
            } else if (column == o.column) {
                return 0
            }
        }
        return 1
    }

    override fun toString(): String {
        return "[" + column + ":" + row + "]"
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 23 * hash + this.row
        hash = 23 * hash + this.column
        return hash
    }

    fun equals(
        columnIndex: Int,
        rowIndex: Int,
    ): Boolean {
        return (this.column == columnIndex && this.row == rowIndex)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (this::class != obj::class) {
            return false
        }
        val other = obj as TerminalPosition
        return this.row == other.row && this.column == other.column
    }

    companion object {
        /**
         * Constant for the top-left corner (0x0)
         */
        val TOP_LEFT_CORNER = TerminalPosition(0, 0)

/**
         * Constant for the 1x1 position (one offset in both directions from top-left)
         */
        val OFFSET_1x1 = TerminalPosition(1, 1)
    }
}
