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
 * Default implementation of ShapeRenderer. This class (and the interface) is mostly here to make the code cleaner in
 * `AbstractTextGraphics`.
 * @author Martin
 */
internal class DefaultShapeRenderer(private val callback: Callback) : ShapeRenderer {
    internal interface Callback {
        fun onPoint(
            column: Int,
            row: Int,
            character: TextCharacter?,
        )
    }

    override fun drawLine(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        character: TextCharacter?,
    ) {
        // Bresenham's line algorithm. References:
        // http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
        // Graphics Programming Black Book (Michael Abrash)
        var point1 = requireNotNull(p1) { "p1" }
        var point2 = requireNotNull(p2) { "p2" }

        if (point1.row > point2.row) {
            val temp = point1
            point1 = point2
            point2 = temp
        }

        var deltaX = point2.column - point1.column
        val deltaY = point2.row - point1.row
        if (deltaX > 0) {
            if (deltaX > deltaY) {
                drawLine0(point1, deltaX, deltaY, true, character)
            } else {
                drawLine1(point1, deltaX, deltaY, true, character)
            }
        } else {
            deltaX = kotlin.math.abs(deltaX)
            if (deltaX > deltaY) {
                drawLine0(point1, deltaX, deltaY, false, character)
            } else {
                drawLine1(point1, deltaX, deltaY, false, character)
            }
        }
    }

    private fun drawLine0(
        start: TerminalPosition,
        deltaX: Int,
        deltaY: Int,
        leftToRight: Boolean,
        character: TextCharacter?,
    ) {
        var remaining = deltaX
        var x = start.column
        var y = start.row
        val deltaYx2 = deltaY * 2
        val deltaYx2MinusDeltaXx2 = deltaYx2 - (deltaX * 2)
        var errorTerm = deltaYx2 - deltaX
        callback.onPoint(x, y, character)
        while (remaining-- > 0) {
            if (errorTerm >= 0) {
                y++
                errorTerm += deltaYx2MinusDeltaXx2
            } else {
                errorTerm += deltaYx2
            }
            x += if (leftToRight) 1 else -1
            callback.onPoint(x, y, character)
        }
    }

    private fun drawLine1(
        start: TerminalPosition,
        deltaX: Int,
        deltaY: Int,
        leftToRight: Boolean,
        character: TextCharacter?,
    ) {
        var remaining = deltaY
        var x = start.column
        var y = start.row
        val deltaXx2 = deltaX * 2
        val deltaXx2MinusDeltaYx2 = deltaXx2 - (deltaY * 2)
        var errorTerm = deltaXx2 - deltaY
        callback.onPoint(x, y, character)
        while (remaining-- > 0) {
            if (errorTerm >= 0) {
                x += if (leftToRight) 1 else -1
                errorTerm += deltaXx2MinusDeltaYx2
            } else {
                errorTerm += deltaXx2
            }
            y++
            callback.onPoint(x, y, character)
        }
    }

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ) {
        val point1 = requireNotNull(p1) { "p1" }
        val point2 = requireNotNull(p2) { "p2" }
        val point3 = requireNotNull(p3) { "p3" }
        drawLine(point1, point2, character)
        drawLine(point2, point3, character)
        drawLine(point3, point1, character)
    }

    override fun drawRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: TextCharacter?,
    ) {
        val origin = requireNotNull(topLeft) { "topLeft" }
        val rectSize = requireNotNull(size) { "size" }
        val topRight = requireNotNull(origin.withRelativeColumn(rectSize.columns - 1))
        val bottomRight = requireNotNull(topRight.withRelativeRow(rectSize.rows - 1))
        val bottomLeft = requireNotNull(origin.withRelativeRow(rectSize.rows - 1))
        drawLine(origin, topRight, character)
        drawLine(topRight, bottomRight, character)
        drawLine(bottomRight, bottomLeft, character)
        drawLine(bottomLeft, origin, character)
    }

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ) {
        // Algorithm reference:
        // http://www-users.mat.uni.torun.pl/~wrona/3d_tutor/tri_fillers.html
        val point1 = requireNotNull(p1) { "p1" }
        val point2 = requireNotNull(p2) { "p2" }
        val point3 = requireNotNull(p3) { "p3" }

        val points = arrayOf(point1, point2, point3)
        points.sortBy { it.row }

        val dx1 =
            if (points[1].row - points[0].row > 0) {
                (points[1].column - points[0].column).toFloat() / (points[1].row - points[0].row).toFloat()
            } else {
                0f
            }
        val dx2 =
            if (points[2].row - points[0].row > 0) {
                (points[2].column - points[0].column).toFloat() / (points[2].row - points[0].row).toFloat()
            } else {
                0f
            }
        val dx3 =
            if (points[2].row - points[1].row > 0) {
                (points[2].column - points[1].column).toFloat() / (points[2].row - points[1].row).toFloat()
            } else {
                0f
            }

        var startX = points[0].column.toFloat()
        var endX = points[0].column.toFloat()
        var startY = points[0].row.toFloat()

        if (dx1 > dx2) {
            while (startY <= points[1].row) {
                drawLine(
                    TerminalPosition(startX.toInt(), startY.toInt()),
                    TerminalPosition(endX.toInt(), startY.toInt()),
                    character,
                )
                startY++
                startX += dx2
                endX += dx1
            }
            endX = points[1].column.toFloat()
            while (startY <= points[2].row) {
                drawLine(
                    TerminalPosition(startX.toInt(), startY.toInt()),
                    TerminalPosition(endX.toInt(), startY.toInt()),
                    character,
                )
                startY++
                startX += dx2
                endX += dx3
            }
        } else {
            while (startY <= points[1].row) {
                drawLine(
                    TerminalPosition(startX.toInt(), startY.toInt()),
                    TerminalPosition(endX.toInt(), startY.toInt()),
                    character,
                )
                startY++
                startX += dx1
                endX += dx2
            }
            startX = points[1].column.toFloat()
            startY = points[1].row.toFloat()
            while (startY <= points[2].row) {
                drawLine(
                    TerminalPosition(startX.toInt(), startY.toInt()),
                    TerminalPosition(endX.toInt(), startY.toInt()),
                    character,
                )
                startY++
                startX += dx3
                endX += dx2
            }
        }
    }

    override fun fillRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: TextCharacter?,
    ) {
        val origin = requireNotNull(topLeft) { "topLeft" }
        val rectSize = requireNotNull(size) { "size" }
        val fill = requireNotNull(character) { "character" }
        val characterDoubleWidth = fill.isDoubleWidth
        for (y in 0 until rectSize.rows) {
            var x = 0
            while (x < rectSize.columns) {
                if (characterDoubleWidth && x + 1 == rectSize.columns) {
                    callback.onPoint(origin.column + x, origin.row + y, fill.withCharacter(' '))
                } else {
                    callback.onPoint(origin.column + x, origin.row + y, fill)
                }
                if (characterDoubleWidth) {
                    x++
                }
                x++
            }
        }
    }
}
