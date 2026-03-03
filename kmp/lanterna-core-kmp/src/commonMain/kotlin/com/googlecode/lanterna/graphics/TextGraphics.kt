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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.ScreenTranslator
import com.googlecode.lanterna.screen.TabBehaviour
import java.util.Collection

interface TextGraphics : StyleSet<TextGraphics>, ScreenTranslator {
    fun getSize(): TerminalSize?

    @Throws(IllegalArgumentException::class)
    fun newTextGraphics(topLeftCorner: TerminalPosition?, size: TerminalSize?): TextGraphics?

    fun getTabBehaviour(): TabBehaviour?

    fun setTabBehaviour(tabBehaviour: TabBehaviour?): TextGraphics?

    fun fill(c: Char): TextGraphics?

    fun setCharacter(column: Int, row: Int, character: Char): TextGraphics?

    fun setCharacter(column: Int, row: Int, character: TextCharacter?): TextGraphics?

    fun setCharacter(position: TerminalPosition?, character: Char): TextGraphics?

    fun setCharacter(position: TerminalPosition?, character: TextCharacter?): TextGraphics?

    fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: Char): TextGraphics?

    fun drawLine(fromPoint: TerminalPosition?, toPoint: TerminalPosition?, character: TextCharacter?): TextGraphics?

    fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: Char): TextGraphics?

    fun drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, character: TextCharacter?): TextGraphics?

    fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char
    ): TextGraphics?

    fun drawTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?
    ): TextGraphics?

    fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: Char
    ): TextGraphics?

    fun fillTriangle(
        p1: TerminalPosition?,
        p2: TerminalPosition?,
        p3: TerminalPosition?,
        character: TextCharacter?
    ): TextGraphics?

    fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics?

    fun drawRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics?

    fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: Char): TextGraphics?

    fun fillRectangle(topLeft: TerminalPosition?, size: TerminalSize?, character: TextCharacter?): TextGraphics?

    fun drawImage(topLeft: TerminalPosition?, image: TextImage?): TextGraphics?

    fun drawImage(
        topLeft: TerminalPosition?,
        image: TextImage?,
        sourceImageTopLeft: TerminalPosition?,
        sourceImageSize: TerminalSize?
    ): TextGraphics?

    fun putString(column: Int, row: Int, string: String?): TextGraphics?

    fun putString(position: TerminalPosition?, string: String?): TextGraphics?

    fun putString(
        column: Int,
        row: Int,
        string: String?,
        extraModifier: SGR?,
        optionalExtraModifiers: Array<out SGR?>?
    ): TextGraphics?

    fun putString(
        position: TerminalPosition?,
        string: String?,
        extraModifier: SGR?,
        optionalExtraModifiers: Array<out SGR?>?
    ): TextGraphics?

    fun putString(column: Int, row: Int, string: String?, extraModifiers: Collection<SGR?>?): TextGraphics?

    fun putCSIStyledString(column: Int, row: Int, string: String?): TextGraphics?

    fun putCSIStyledString(position: TerminalPosition?, string: String?): TextGraphics?

    fun getCharacter(position: TerminalPosition?): TextCharacter?

    fun getCharacter(column: Int, row: Int): TextCharacter?
}
