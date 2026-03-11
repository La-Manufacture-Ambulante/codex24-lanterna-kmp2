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
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.TabBehaviour
import com.googlecode.lanterna.internal.compat.EnumSet

/**
 * Default logic for TextGraphics implementations.
 */
abstract class AbstractTextGraphics protected constructor() : TextGraphics {
    private val activeModifiersBacking: EnumSet<SGR> = EnumSet.noneOf(SGR::class)

    override var foregroundColor: TextColor? = TextColor.ANSI.DEFAULT
    override var backgroundColor: TextColor? = TextColor.ANSI.DEFAULT
    override var tabBehaviour: TabBehaviour? = TabBehaviour.ALIGN_TO_COLUMN_4

    override val activeModifiers: EnumSet<SGR>?
        get() = EnumSet.copyOf(activeModifiersBacking)

    private val shapeRenderer: ShapeRenderer = DefaultShapeRenderer(
        object : DefaultShapeRenderer.Callback {
            override fun onPoint(column: Int, row: Int, character: TextCharacter?) {
                this@AbstractTextGraphics.setCharacter(column, row, character)
            }
        },
    )

    protected open val screenLocation: TerminalPosition
        get() = TerminalPosition.TOP_LEFT_CORNER

    override fun setBackgroundColor(backgroundColor: TextColor?): TextGraphics? {
        this.backgroundColor = backgroundColor
        return this
    }

    override fun setForegroundColor(foregroundColor: TextColor?): TextGraphics? {
        this.foregroundColor = foregroundColor
        return this
    }

    override fun enableModifiers(vararg modifiers: SGR?): TextGraphics? {
        for (modifier in modifiers) {
            if (modifier != null) {
                activeModifiersBacking.add(modifier)
            }
        }
        return this
    }

    override fun disableModifiers(vararg modifiers: SGR?): TextGraphics? {
        for (modifier in modifiers) {
            if (modifier != null) {
                activeModifiersBacking.remove(modifier)
            }
        }
        return this
    }

    override fun setModifiers(modifiers: EnumSet<SGR>?): TextGraphics? {
        activeModifiersBacking.clear()
        if (modifiers != null) {
            for (modifier in modifiers) {
                if (modifier != null) {
                    activeModifiersBacking.add(modifier)
                }
            }
        }
        return this
    }

    override fun clearModifiers(): TextGraphics? {
        activeModifiersBacking.clear()
        return this
    }

    override fun setTabBehaviour(tabBehaviour: TabBehaviour?): TextGraphics? {
        if (tabBehaviour != null) {
            this.tabBehaviour = tabBehaviour
        }
        return this
    }

    override fun fill(c: Char): TextGraphics? {
        fillRectangle(TerminalPosition.TOP_LEFT_CORNER, size, c)
        return this
    }

    override fun setCharacter(column: Int, row: Int, character: Char): TextGraphics? {
        return setCharacter(column, row, newTextCharacter(character))
    }

    override fun setCharacter(position: TerminalPosition?, character: TextCharacter?): TextGraphics? {
        if (position != null) {
            setCharacter(position.column, position.row, character)
        }
        return this
    }

    override fun setCharacter(position: TerminalPosition?, character: Char): TextGraphics? {
        return if (position == null) this else setCharacter(position.column, position.row, character)
    }

    override fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: Char): TextGraphics? {
        return drawLine(fromPoint, toPoint, newTextCharacter(character))
    }

    override fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: TextCharacter?): TextGraphics? {
        if (fromPoint != null && toPoint != null) {
            shapeRenderer.drawLine(fromPoint, toPoint, character)
        }
        return this
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: Char): TextGraphics? {
        return drawLine(fromX, fromY, toX, toY, newTextCharacter(character))
    }

    override fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: TextCharacter?): TextGraphics? {
        return drawLine(TerminalPosition(fromX, fromY), TerminalPosition(toX, toY), character)
    }

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char,
    ): TextGraphics? = drawTriangle(p1, p2, p3, newTextCharacter(character))

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics? {
        shapeRenderer.drawTriangle(p1, p2, p3, character)
        return this
    }

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char,
    ): TextGraphics? = fillTriangle(p1, p2, p3, newTextCharacter(character))

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics? {
        shapeRenderer.fillTriangle(p1, p2, p3, character)
        return this
    }

    override fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics? {
        return drawRectangle(topLeft, size, newTextCharacter(character))
    }

    override fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics? {
        if (topLeft != null && size != null) {
            shapeRenderer.drawRectangle(topLeft, size, character)
        }
        return this
    }

    override fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics? {
        return fillRectangle(topLeft, size, newTextCharacter(character))
    }

    override fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics? {
        if (topLeft != null && size != null && character != null) {
            shapeRenderer.fillRectangle(topLeft, size, character)
        }
        return this
    }

    override fun drawImage(topLeft: TerminalPosition?, image: TextImage?): TextGraphics? {
        return drawImage(topLeft, image, TerminalPosition.TOP_LEFT_CORNER, image?.size)
    }

    override fun drawImage(
        topLeft: TerminalPosition?,
        image: TextImage?,
        sourceImageTopLeft: TerminalPosition?,
        sourceImageSize: TerminalSize?,
    ): TextGraphics? {
        var dstTopLeft = topLeft ?: return this
        var srcTopLeft = sourceImageTopLeft ?: TerminalPosition.TOP_LEFT_CORNER
        var srcSize = sourceImageSize ?: image?.size ?: return this
        val srcImage = image ?: return this
        val dstSize = size ?: return this

        if (srcTopLeft.column < 0) {
            dstTopLeft = dstTopLeft.withRelativeColumn(-srcTopLeft.column) ?: dstTopLeft
            srcSize = srcSize.withRelativeColumns(srcTopLeft.column) ?: srcSize
            srcTopLeft = srcTopLeft.withColumn(0) ?: srcTopLeft
        }
        if (srcTopLeft.row < 0) {
            dstTopLeft = dstTopLeft.withRelativeRow(-srcTopLeft.row) ?: dstTopLeft
            srcSize = srcSize.withRelativeRows(srcTopLeft.row) ?: srcSize
            srcTopLeft = srcTopLeft.withRow(0) ?: srcTopLeft
        }

        var fromRow = maxOf(srcTopLeft.row, 0)
        var untilRow = minOf(srcTopLeft.row + srcSize.rows, srcImage.size?.rows ?: 0)
        var fromColumn = maxOf(srcTopLeft.column, 0)
        var untilColumn = minOf(srcTopLeft.column + srcSize.columns, srcImage.size?.columns ?: 0)

        val diffRow = dstTopLeft.row - srcTopLeft.row
        val diffColumn = dstTopLeft.column - srcTopLeft.column

        fromRow = maxOf(fromRow, -diffRow)
        fromColumn = maxOf(fromColumn, -diffColumn)
        untilRow = minOf(untilRow, dstSize.rows - diffRow)
        untilColumn = minOf(untilColumn, dstSize.columns - diffColumn)

        if (fromRow >= untilRow || fromColumn >= untilColumn) {
            return this
        }

        for (row in fromRow until untilRow) {
            for (column in fromColumn until untilColumn) {
                setCharacter(column + diffColumn, row + diffRow, srcImage.getCharacterAt(column, row))
            }
        }
        return this
    }

    override fun putString(column: Int, row: Int, string: String?): TextGraphics? {
        val prepared = prepareStringForPut(column, string ?: "")
        var offset = 0
        for (character in prepared) {
            setCharacter(column + offset, row, newTextCharacter(character))
            offset += getOffsetToNextCharacter(character)
        }
        return this
    }

    override fun putString(position: TerminalPosition?, string: String?): TextGraphics? {
        if (position != null) {
            putString(position.column, position.row, string)
        }
        return this
    }

    override fun putString(
        column: Int,
        row: Int,
        string: String?,
        extraModifier: SGR?,
        vararg optionalExtraModifiers: SGR?,
    ): TextGraphics? {
        clearModifiers()
        if (extraModifier != null) {
            val all = arrayOfNulls<SGR>(optionalExtraModifiers.size + 1)
            all[0] = extraModifier
            for (i in optionalExtraModifiers.indices) {
                all[i + 1] = optionalExtraModifiers[i]
            }
            val set = EnumSet.noneOf(SGR::class)
            for (modifier in all) {
                if (modifier != null) {
                    set.add(modifier)
                }
            }
            return putString(column, row, string, set)
        }
        return putString(column, row, string)
    }

    override fun putString(column: Int, row: Int, string: String?, extraModifiers: Collection<SGR?>?): TextGraphics? {
        val newModifiers = EnumSet.noneOf(SGR::class)
        if (extraModifiers != null) {
            for (modifier in extraModifiers) {
                if (modifier != null && !activeModifiersBacking.contains(modifier)) {
                    newModifiers.add(modifier)
                }
            }
        }
        if (newModifiers.isNotEmpty()) {
            enableModifiers(*newModifiers.toTypedArray())
        }
        putString(column, row, string)
        if (newModifiers.isNotEmpty()) {
            disableModifiers(*newModifiers.toTypedArray())
        }
        return this
    }

    override fun putString(
        position: TerminalPosition?,
        string: String?,
        extraModifier: SGR?,
        vararg optionalExtraModifiers: SGR?,
    ): TextGraphics? {
        if (position != null) {
            putString(position.column, position.row, string, extraModifier, *optionalExtraModifiers)
        }
        return this
    }

    override fun putCSIStyledString(column: Int, row: Int, string: String?): TextGraphics? {
        val original = StyleSet.Set(this)
        val prepared = prepareStringForPut(column, string ?: "")
        var offset = 0
        var i = 0
        while (i < prepared.length) {
            val character = prepared[i]
            val controlSequence = TerminalTextUtils.getANSIControlSequenceAt(prepared, i)
            if (controlSequence != null) {
                TerminalTextUtils.updateModifiersFromCSICode(controlSequence, this, original)
                i += controlSequence.length
                continue
            }
            setCharacter(column + offset, row, newTextCharacter(character))
            offset += getOffsetToNextCharacter(character)
            i++
        }
        setStyleFrom(original)
        return this
    }

    override fun putCSIStyledString(position: TerminalPosition?, string: String?): TextGraphics? {
        return if (position == null) this else putCSIStyledString(position.column, position.row, string)
    }

    override fun getCharacter(position: TerminalPosition?): TextCharacter? {
        return if (position == null) null else getCharacter(position.column, position.row)
    }

    override fun toScreenPosition(pos: TerminalPosition?): TerminalPosition? {
        if (pos == null) {
            return null
        }
        val sz = size ?: return null
        val max = screenLocation.plus(TerminalPosition(sz.columns - 1, sz.rows - 1)) ?: return null
        val loc = screenLocation.plus(pos) ?: return null
        return if (loc.column > max.column || loc.row > max.row) null else loc
    }

    @Throws(IllegalArgumentException::class)
    override fun newTextGraphics(topLeftCorner: TerminalPosition?, size: TerminalSize?): TextGraphics? {
        if (topLeftCorner == null || size == null) {
            return this
        }
        val writableArea = this.size ?: return NullTextGraphics(size)
        if (
            (topLeftCorner.column + size.columns <= 0) ||
            topLeftCorner.column >= writableArea.columns ||
            (topLeftCorner.row + size.rows <= 0) ||
            topLeftCorner.row >= writableArea.rows
        ) {
            return NullTextGraphics(size)
        }
        return SubTextGraphics(this, topLeftCorner, screenLocation, size)
    }

    private fun newTextCharacter(character: Char): TextCharacter {
        return TextCharacter(
            character,
            foregroundColor,
            backgroundColor,
            EnumSet.copyOf(activeModifiersBacking),
        )
    }

    private fun prepareStringForPut(column: Int, string: String): String {
        var out = string
        if (out.contains("\n")) {
            out = out.substring(0, out.indexOf("\n"))
        }
        if (out.contains("\r")) {
            out = out.substring(0, out.indexOf("\r"))
        }
        return tabBehaviour!!.replaceTabs(out, column) ?: out
    }

    private fun getOffsetToNextCharacter(character: Char): Int {
        return if (TerminalTextUtils.isCharDoubleWidth(character)) 2 else 1
    }

    override fun setStyleFrom(source: StyleSet<*>?): TextGraphics? {
        if (source == null) {
            return this
        }
        setBackgroundColor(source.backgroundColor)
        setForegroundColor(source.foregroundColor)
        setModifiers(source.activeModifiers)
        return this
    }
}
