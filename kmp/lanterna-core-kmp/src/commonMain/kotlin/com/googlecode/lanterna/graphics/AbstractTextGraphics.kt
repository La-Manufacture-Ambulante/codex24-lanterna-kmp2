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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.StyleSet
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextImage
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.Arrays
import java.util.Collection
import java.util.EnumSet
import kotlin.math.max
import kotlin.math.min

abstract class AbstractTextGraphics protected constructor() : TextGraphics {
    protected var foregroundColor: TextColor? = TextColor.ANSI.DEFAULT
    protected var backgroundColor: TextColor? = TextColor.ANSI.DEFAULT
    protected var tabBehaviour: TabBehaviour = TabBehaviour.ALIGN_TO_COLUMN_4
    protected val activeModifiers: EnumSet<SGR> = EnumSet.noneOf(SGR::class.java)
    private val shapeRenderer: ShapeRenderer = DefaultShapeRenderer { column, row, character ->
        setCharacter(column, row, character)
    }

    override fun getBackgroundColor(): TextColor? {
        return backgroundColor
    }

    override fun setBackgroundColor(backgroundColor: TextColor?): TextGraphics {
        this.backgroundColor = backgroundColor
        return this
    }

    override fun getForegroundColor(): TextColor? {
        return foregroundColor
    }

    override fun setForegroundColor(foregroundColor: TextColor?): TextGraphics {
        this.foregroundColor = foregroundColor
        return this
    }

    override fun enableModifiers(modifiers: Array<out SGR>?): TextGraphics {
        val modifierArray = modifiers ?: throw NullPointerException("modifiers")
        enableModifiers(Arrays.asList(*modifierArray))
        return this
    }

    private fun enableModifiers(modifiers: Collection<SGR>) {
        activeModifiers.addAll(modifiers)
    }

    override fun disableModifiers(modifiers: Array<out SGR>?): TextGraphics {
        val modifierArray = modifiers ?: throw NullPointerException("modifiers")
        disableModifiers(Arrays.asList(*modifierArray))
        return this
    }

    private fun disableModifiers(modifiers: Collection<SGR>) {
        activeModifiers.removeAll(modifiers)
    }

    @Synchronized
    override fun setModifiers(modifiers: EnumSet<SGR>): TextGraphics {
        activeModifiers.clear()
        activeModifiers.addAll(modifiers)
        return this
    }

    override fun clearModifiers(): TextGraphics {
        activeModifiers.clear()
        return this
    }

    override fun getActiveModifiers(): EnumSet<SGR> {
        return activeModifiers
    }

    override fun getTabBehaviour(): TabBehaviour {
        return tabBehaviour
    }

    override fun setTabBehaviour(tabBehaviour: TabBehaviour?): TextGraphics {
        if (tabBehaviour != null) {
            this.tabBehaviour = tabBehaviour
        }
        return this
    }

    override fun fill(c: Char): TextGraphics {
        fillRectangle(TerminalPosition.TOP_LEFT_CORNER, getSize(), c)
        return this
    }

    override fun setCharacter(column: Int, row: Int, character: Char): TextGraphics {
        return setCharacter(column, row, newTextCharacter(character))
    }

    override fun setCharacter(position: TerminalPosition, textCharacter: TextCharacter): TextGraphics {
        setCharacter(position.column, position.row, textCharacter)
        return this
    }

    override fun setCharacter(position: TerminalPosition, character: Char): TextGraphics {
        return setCharacter(position.column, position.row, character)
    }

    override fun drawLine(fromPosition: TerminalPosition, toPoint: TerminalPosition, character: Char): TextGraphics {
        return drawLine(fromPosition, toPoint, newTextCharacter(character))
    }

    override fun drawLine(fromPoint: TerminalPosition, toPoint: TerminalPosition, character: TextCharacter): TextGraphics {
        shapeRenderer.drawLine(fromPoint, toPoint, character)
        return this
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: Char): TextGraphics {
        return drawLine(fromX, fromY, toX, toY, newTextCharacter(character))
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: TextCharacter): TextGraphics {
        return drawLine(TerminalPosition(fromX, fromY), TerminalPosition(toX, toY), character)
    }

    override fun drawTriangle(
        p1: TerminalPosition,
        p2: TerminalPosition,
        p3: TerminalPosition,
        character: Char
    ): TextGraphics {
        return drawTriangle(p1, p2, p3, newTextCharacter(character))
    }

    override fun drawTriangle(
        p1: TerminalPosition,
        p2: TerminalPosition,
        p3: TerminalPosition,
        character: TextCharacter
    ): TextGraphics {
        shapeRenderer.drawTriangle(p1, p2, p3, character)
        return this
    }

    override fun fillTriangle(
        p1: TerminalPosition,
        p2: TerminalPosition,
        p3: TerminalPosition,
        character: Char
    ): TextGraphics {
        return fillTriangle(p1, p2, p3, newTextCharacter(character))
    }

    override fun fillTriangle(
        p1: TerminalPosition,
        p2: TerminalPosition,
        p3: TerminalPosition,
        character: TextCharacter
    ): TextGraphics {
        shapeRenderer.fillTriangle(p1, p2, p3, character)
        return this
    }

    override fun drawRectangle(topLeft: TerminalPosition, size: TerminalSize, character: Char): TextGraphics {
        return drawRectangle(topLeft, size, newTextCharacter(character))
    }

    override fun drawRectangle(topLeft: TerminalPosition, size: TerminalSize, character: TextCharacter): TextGraphics {
        shapeRenderer.drawRectangle(topLeft, size, character)
        return this
    }

    override fun fillRectangle(topLeft: TerminalPosition, size: TerminalSize, character: Char): TextGraphics {
        return fillRectangle(topLeft, size, newTextCharacter(character))
    }

    override fun fillRectangle(topLeft: TerminalPosition, size: TerminalSize, character: TextCharacter): TextGraphics {
        shapeRenderer.fillRectangle(topLeft, size, character)
        return this
    }

    override fun drawImage(topLeft: TerminalPosition, image: TextImage): TextGraphics {
        return drawImage(topLeft, image, TerminalPosition.TOP_LEFT_CORNER, image.size)
    }

    override fun drawImage(
        topLeft: TerminalPosition,
        image: TextImage,
        sourceImageTopLeft: TerminalPosition,
        sourceImageSize: TerminalSize
    ): TextGraphics {
        var mutableTopLeft = topLeft
        var mutableSourceImageTopLeft = sourceImageTopLeft
        var mutableSourceImageSize = sourceImageSize

        if (mutableSourceImageTopLeft.column < 0) {
            mutableTopLeft = mutableTopLeft.withRelativeColumn(-mutableSourceImageTopLeft.column)
            mutableSourceImageSize = mutableSourceImageSize.withRelativeColumns(mutableSourceImageTopLeft.column)
            mutableSourceImageTopLeft = mutableSourceImageTopLeft.withColumn(0)
        }
        if (mutableSourceImageTopLeft.row < 0) {
            mutableTopLeft = mutableTopLeft.withRelativeRow(-mutableSourceImageTopLeft.row)
            mutableSourceImageSize = mutableSourceImageSize.withRelativeRows(mutableSourceImageTopLeft.row)
            mutableSourceImageTopLeft = mutableSourceImageTopLeft.withRow(0)
        }

        var fromRow = max(mutableSourceImageTopLeft.row, 0)
        var untilRow = min(mutableSourceImageTopLeft.row + mutableSourceImageSize.rows, image.size.rows)
        var fromColumn = max(mutableSourceImageTopLeft.column, 0)
        var untilColumn = min(mutableSourceImageTopLeft.column + mutableSourceImageSize.columns, image.size.columns)

        val diffRow = mutableTopLeft.row - mutableSourceImageTopLeft.row
        val diffColumn = mutableTopLeft.column - mutableSourceImageTopLeft.column

        fromRow = max(fromRow, -diffRow)
        fromColumn = max(fromColumn, -diffColumn)

        untilRow = min(untilRow, getSize().rows - diffRow)
        untilColumn = min(untilColumn, getSize().columns - diffColumn)

        if (fromRow >= untilRow || fromColumn >= untilColumn) {
            return this
        }
        for (row in fromRow until untilRow) {
            for (column in fromColumn until untilColumn) {
                setCharacter(column + diffColumn, row + diffRow, image.getCharacterAt(column, row))
            }
        }
        return this
    }

    override fun putString(column: Int, row: Int, string: String): TextGraphics {
        val prepared = prepareStringForPut(column, string)
        var offset = 0
        for (i in 0 until prepared.length) {
            val character = prepared[i]
            setCharacter(column + offset, row, newTextCharacter(character))
            offset += getOffsetToNextCharacter(character)
        }
        return this
    }

    override fun putString(position: TerminalPosition, string: String): TextGraphics {
        putString(position.column, position.row, string)
        return this
    }

    override fun putString(
        column: Int,
        row: Int,
        string: String,
        extraModifier: SGR,
        optionalExtraModifiers: Array<out SGR>?
    ): TextGraphics {
        val extra = optionalExtraModifiers ?: throw NullPointerException("optionalExtraModifiers")
        clearModifiers()
        return putString(column, row, string, EnumSet.of(extraModifier, *extra))
    }

    override fun putString(column: Int, row: Int, string: String, extraModifiers: Collection<SGR>): TextGraphics {
        val newModifiers: Collection<SGR> = EnumSet.copyOf(extraModifiers)
        val mutableNewModifiers = newModifiers.toMutableSet()
        mutableNewModifiers.removeAll(activeModifiers)
        enableModifiers(mutableNewModifiers)
        putString(column, row, string)
        disableModifiers(mutableNewModifiers)
        return this
    }

    override fun putString(
        position: TerminalPosition,
        string: String,
        extraModifier: SGR,
        optionalExtraModifiers: Array<out SGR>?
    ): TextGraphics {
        putString(position.column, position.row, string, extraModifier, optionalExtraModifiers)
        return this
    }

    @Synchronized
    override fun putCSIStyledString(column: Int, row: Int, string: String): TextGraphics {
        val original = StyleSet.Set(this)
        val prepared = prepareStringForPut(column, string)
        var offset = 0
        var i = 0
        while (i < prepared.length) {
            val character = prepared[i]
            val controlSequence = TerminalTextUtils.getANSIControlSequenceAt(prepared, i)
            if (controlSequence != null) {
                TerminalTextUtils.updateModifiersFromCSICode(controlSequence, this, original)
                i += controlSequence.length - 1
                i++
                continue
            }

            setCharacter(column + offset, row, newTextCharacter(character))
            offset += getOffsetToNextCharacter(character)
            i++
        }

        setStyleFrom(original)
        return this
    }

    override fun putCSIStyledString(position: TerminalPosition, string: String): TextGraphics {
        return putCSIStyledString(position.column, position.row, string)
    }

    override fun getCharacter(position: TerminalPosition): TextCharacter {
        return getCharacter(position.column, position.row)
    }

    protected open fun getScreenLocation(): TerminalPosition {
        return TerminalPosition.TOP_LEFT_CORNER
    }

    override fun toScreenPosition(pos: TerminalPosition): TerminalPosition? {
        val max = getScreenLocation().plus(TerminalPosition(getSize().columns - 1, getSize().rows - 1))
        val loc = getScreenLocation().plus(pos)
        return if (loc.column > max.column || loc.row > max.row) {
            null
        } else {
            loc
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun newTextGraphics(topLeftCorner: TerminalPosition, size: TerminalSize): TextGraphics {
        val writableArea = getSize()
        if (topLeftCorner.column + size.columns <= 0 ||
            topLeftCorner.column >= writableArea.columns ||
            topLeftCorner.row + size.rows <= 0 ||
            topLeftCorner.row >= writableArea.rows
        ) {
            return NullTextGraphics(size)
        }
        return SubTextGraphics(this, topLeftCorner, getScreenLocation(), size)
    }

    private fun newTextCharacter(character: Char): TextCharacter {
        return TextCharacter(character, foregroundColor, backgroundColor, activeModifiers)
    }

    private fun prepareStringForPut(column: Int, string: String): String {
        var prepared = string
        if (prepared.contains("\n")) {
            prepared = prepared.substring(0, prepared.indexOf("\n"))
        }
        if (prepared.contains("\r")) {
            prepared = prepared.substring(0, prepared.indexOf("\r"))
        }
        prepared = tabBehaviour.replaceTabs(prepared, column)
        return prepared
    }

    private fun getOffsetToNextCharacter(character: Char): Int {
        return if (TerminalTextUtils.isCharDoubleWidth(character)) {
            2
        } else {
            1
        }
    }

    override fun setStyleFrom(source: StyleSet<*>): TextGraphics {
        setBackgroundColor(source.backgroundColor)
        setForegroundColor(source.foregroundColor)
        setModifiers(source.activeModifiers)
        return this
    }
}
