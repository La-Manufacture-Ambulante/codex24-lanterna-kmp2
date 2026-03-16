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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemedTextGraphics

/**
 * WindowPostRenderer implementation that draws a shadow under the window.
 */
class WindowShadowRenderer : WindowPostRenderer {
    override fun postRender(
        textGraphics: ThemedTextGraphics?,
        textGUI: TextGUI?,
        window: Window?,
    ) {
        val graphics = textGraphics ?: return
        val activeWindow = window ?: return
        val windowPosition = activeWindow.position ?: return
        val decoratedWindowSize = activeWindow.decoratedSize ?: return
        val themeDefinition = activeWindow.theme?.getDefinition(WindowShadowRenderer::class) ?: return

        graphics.applyThemeStyle(themeDefinition.normal)
        val filler = themeDefinition.getCharacter("FILLER", ' ')
        val useDoubleWidth = themeDefinition.getBooleanProperty("DOUBLE_WIDTH", true)
        val useTransparency = themeDefinition.getBooleanProperty("TRANSPARENT", false)

        val lowerLeft =
            windowPosition.withRelativeColumn(if (useDoubleWidth) 2 else 1)!!
                .withRelativeRow(decoratedWindowSize.rows)!!
        var lowerRight = lowerLeft.withRelativeColumn(decoratedWindowSize.columns - if (useDoubleWidth) 3 else 2)!!
        var column = lowerLeft.column
        while (column <= lowerRight.column + 1) {
            var characterToDraw = filler
            if (useTransparency) {
                val tc = graphics.getCharacter(column, lowerLeft.row)
                if (tc != null) {
                    characterToDraw = tc.characterString[0]
                }
            }
            graphics.setCharacter(column, lowerLeft.row, characterToDraw)
            if (TerminalTextUtils.isCharDoubleWidth(characterToDraw)) {
                column++
            }
            column++
        }

        lowerRight = lowerRight.withRelativeColumn(1)!!
        var upperRight = lowerRight.withRelativeRow(-decoratedWindowSize.rows + 1)!!
        var hasDoubleWidthShadow = false
        for (row in upperRight.row until lowerRight.row) {
            var characterToDraw = filler
            if (useTransparency) {
                val tc = graphics.getCharacter(upperRight.column, row)
                if (tc != null) {
                    characterToDraw = tc.characterString[0]
                }
            }
            graphics.setCharacter(upperRight.column, row, characterToDraw)
            if (TerminalTextUtils.isCharDoubleWidth(characterToDraw)) {
                hasDoubleWidthShadow = true
            }
        }

        graphics.applyThemeStyle(themeDefinition.normal)
        if (useDoubleWidth || hasDoubleWidthShadow) {
            upperRight = upperRight.withRelativeColumn(1)!!
            for (row in upperRight.row..lowerRight.row) {
                var characterToDraw = filler
                if (useTransparency) {
                    val tc = graphics.getCharacter(upperRight.column, row)
                    if (tc != null && !tc.isDoubleWidth) {
                        characterToDraw = tc.characterString[0]
                    }
                }
                val neighbour = graphics.getCharacter(upperRight.column - 1, row)
                if (neighbour != null && !neighbour.isDoubleWidth) {
                    graphics.setCharacter(upperRight.column, row, characterToDraw)
                }
            }
        }
    }
}
