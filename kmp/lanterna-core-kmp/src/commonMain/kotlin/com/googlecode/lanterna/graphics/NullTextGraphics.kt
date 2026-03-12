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
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.internal.compat.EnumSet
import com.googlecode.lanterna.screen.TabBehaviour

/**
 * TextGraphics implementation that does nothing, but has a pre-defined size.
 */
internal class NullTextGraphics(override val size: TerminalSize?) : TextGraphics {
    override var foregroundColor: TextColor? = TextColor.ANSI.DEFAULT
    override var backgroundColor: TextColor? = TextColor.ANSI.DEFAULT
    override var tabBehaviour: TabBehaviour? = TabBehaviour.ALIGN_TO_COLUMN_4

    private val styleSet: EnumSet<SGR> = EnumSet.noneOf(SGR::class)

    override val activeModifiers: EnumSet<SGR>
        get() = EnumSet.copyOf(styleSet)

    override fun toScreenPosition(pos: TerminalPosition?): TerminalPosition? {
        return null
    }

    override fun newTextGraphics(
        topLeftCorner: TerminalPosition?,
        size: TerminalSize?,
    ): TextGraphics {
        return this
    }

    override fun setBackgroundColor(backgroundColor: TextColor?): TextGraphics {
        this.backgroundColor = backgroundColor
        return this
    }

    override fun setForegroundColor(foregroundColor: TextColor?): TextGraphics {
        this.foregroundColor = foregroundColor
        return this
    }

    override fun enableModifiers(vararg modifiers: SGR?): TextGraphics {
        styleSet.addAll(listOf(*modifiers).filterNotNull())
        return this
    }

    override fun disableModifiers(vararg modifiers: SGR?): TextGraphics {
        styleSet.removeAll(listOf(*modifiers).filterNotNull().toSet())
        return this
    }

    override fun setModifiers(modifiers: EnumSet<SGR>?): TextGraphics {
        clearModifiers()
        if (modifiers != null) {
            styleSet.addAll(modifiers)
        }
        return this
    }

    override fun clearModifiers(): TextGraphics {
        styleSet.clear()
        return this
    }

    override fun setTabBehaviour(tabBehaviour: TabBehaviour?): TextGraphics {
        this.tabBehaviour = tabBehaviour
        return this
    }

    override fun fill(c: Char): TextGraphics = this

    override fun setCharacter(
        column: Int,
        row: Int,
        character: Char,
    ): TextGraphics = this

    override fun setCharacter(
        column: Int,
        row: Int,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun setCharacter(
        position: TerminalPosition?,
        character: Char,
    ): TextGraphics = this

    override fun setCharacter(
        position: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun drawLine(
        fromPoint: TerminalPosition?,
        toPoint: TerminalPosition?,
        character: Char,
    ): TextGraphics = this

    override fun drawLine(
        fromPoint: TerminalPosition?,
        toPoint: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun drawLine(
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
        character: Char,
    ): TextGraphics = this

    override fun drawLine(
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char,
    ): TextGraphics = this

    override fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char,
    ): TextGraphics = this

    override fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun drawRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: Char,
    ): TextGraphics = this

    override fun drawRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun fillRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: Char,
    ): TextGraphics = this

    override fun fillRectangle(
        topLeft: TerminalPosition?,
        size: TerminalSize?,
        character: TextCharacter?,
    ): TextGraphics = this

    override fun drawImage(
        topLeft: TerminalPosition?,
        image: TextImage?,
    ): TextGraphics = this

    override fun drawImage(
        topLeft: TerminalPosition?,
        image: TextImage?,
        sourceImageTopLeft: TerminalPosition?,
        sourceImageSize: TerminalSize?,
    ): TextGraphics = this

    override fun putString(
        column: Int,
        row: Int,
        string: String?,
    ): TextGraphics = this

    override fun putString(
        position: TerminalPosition?,
        string: String?,
    ): TextGraphics = this

    override fun putString(
        column: Int,
        row: Int,
        string: String?,
        extraModifier: SGR?,
        vararg optionalExtraModifiers: SGR?,
    ): TextGraphics = this

    override fun putString(
        position: TerminalPosition?,
        string: String?,
        extraModifier: SGR?,
        vararg optionalExtraModifiers: SGR?,
    ): TextGraphics = this

    override fun putString(
        column: Int,
        row: Int,
        string: String?,
        extraModifiers: kotlin.collections.Collection<SGR?>?,
    ): TextGraphics = this

    override fun putCSIStyledString(
        column: Int,
        row: Int,
        string: String?,
    ): TextGraphics = this

    override fun putCSIStyledString(
        position: TerminalPosition?,
        string: String?,
    ): TextGraphics = this

    override fun getCharacter(
        column: Int,
        row: Int,
    ): TextCharacter? = null

    override fun getCharacter(position: TerminalPosition?): TextCharacter? = null

    override fun setStyleFrom(source: StyleSet<*>?): TextGraphics {
        if (source != null) {
            setBackgroundColor(source.backgroundColor)
            setForegroundColor(source.foregroundColor)
            setModifiers(source.activeModifiers)
        }
        return this
    }
}
