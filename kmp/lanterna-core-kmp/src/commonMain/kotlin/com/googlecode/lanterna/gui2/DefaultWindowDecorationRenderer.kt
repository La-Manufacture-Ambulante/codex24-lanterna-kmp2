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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * Default window decoration renderer.
 */
class DefaultWindowDecorationRenderer : WindowDecorationRenderer {

    override fun draw(textGUI: WindowBasedTextGUI?, graphics: TextGUIGraphics?, window: Window?): TextGUIGraphics? {
        val w = window ?: return graphics
        val g = graphics ?: return null

        val title = w.title ?: ""
        val drawableArea = g.size ?: TerminalSize.ZERO
        val themeDefinition: ThemeDefinition = w.theme?.getDefinition(DefaultWindowDecorationRenderer::class.java) ?: return g
        val horizontalLine = themeDefinition.getCharacter("HORIZONTAL_LINE", Symbols.SINGLE_LINE_HORIZONTAL)
        val verticalLine = themeDefinition.getCharacter("VERTICAL_LINE", Symbols.SINGLE_LINE_VERTICAL)
        val bottomLeftCorner = themeDefinition.getCharacter("BOTTOM_LEFT_CORNER", Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
        val topLeftCorner = themeDefinition.getCharacter("TOP_LEFT_CORNER", Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
        val bottomRightCorner = themeDefinition.getCharacter("BOTTOM_RIGHT_CORNER", Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
        val topRightCorner = themeDefinition.getCharacter("TOP_RIGHT_CORNER", Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
        val titleSeparatorLeft = themeDefinition.getCharacter("TITLE_SEPARATOR_LEFT", Symbols.SINGLE_LINE_HORIZONTAL)
        val titleSeparatorRight = themeDefinition.getCharacter("TITLE_SEPARATOR_RIGHT", Symbols.SINGLE_LINE_HORIZONTAL)
        val useTitlePadding = themeDefinition.getBooleanProperty("TITLE_PADDING", false)
        val centerTitle = themeDefinition.getBooleanProperty("CENTER_TITLE", false)

        var titleHorizontalPosition = if (useTitlePadding) TITLE_POSITION_WITH_PADDING else TITLE_POSITION_WITHOUT_PADDING
        val titleMaxColumns = drawableArea.columns - titleHorizontalPosition * 2
        if (centerTitle) {
            titleHorizontalPosition = (drawableArea.columns / 2) - (TerminalTextUtils.getColumnWidth(title) / 2)
            titleHorizontalPosition = kotlin.math.max(
                titleHorizontalPosition,
                if (useTitlePadding) TITLE_POSITION_WITH_PADDING else TITLE_POSITION_WITHOUT_PADDING,
            )
        }
        val actualTitle = TerminalTextUtils.fitString(title, titleMaxColumns) ?: ""
        val titleActualColumns = TerminalTextUtils.getColumnWidth(actualTitle)

        if (w.hints?.contains(Window.Hint.MENU_POPUP) == true) {
            g.applyThemeStyle(themeDefinition.normal)
        } else {
            g.applyThemeStyle(themeDefinition.preLight)
        }
        g.drawLine(TerminalPosition(0, drawableArea.rows - 2), TerminalPosition(0, 1), verticalLine)
        g.drawLine(TerminalPosition(1, 0), TerminalPosition(drawableArea.columns - 2, 0), horizontalLine)
        g.setCharacter(0, 0, topLeftCorner)
        g.setCharacter(0, drawableArea.rows - 1, bottomLeftCorner)

        if (actualTitle.isNotEmpty() && drawableArea.columns > 8) {
            var separatorOffset = 1
            if (useTitlePadding) {
                g.setCharacter(titleHorizontalPosition - 1, 0, ' ')
                g.setCharacter(titleHorizontalPosition + titleActualColumns, 0, ' ')
                separatorOffset = 2
            }
            g.setCharacter(titleHorizontalPosition - separatorOffset, 0, titleSeparatorLeft)
            g.setCharacter(titleHorizontalPosition + titleActualColumns + separatorOffset - 1, 0, titleSeparatorRight)
        }

        g.applyThemeStyle(themeDefinition.normal)
        g.drawLine(
            TerminalPosition(drawableArea.columns - 1, 1),
            TerminalPosition(drawableArea.columns - 1, drawableArea.rows - 2),
            verticalLine,
        )
        g.drawLine(
            TerminalPosition(1, drawableArea.rows - 1),
            TerminalPosition(drawableArea.columns - 2, drawableArea.rows - 1),
            horizontalLine,
        )
        g.setCharacter(drawableArea.columns - 1, 0, topRightCorner)
        g.setCharacter(drawableArea.columns - 1, drawableArea.rows - 1, bottomRightCorner)

        if (actualTitle.isNotEmpty()) {
            if (textGUI?.activeWindow === w) {
                g.applyThemeStyle(themeDefinition.active)
            } else {
                g.applyThemeStyle(themeDefinition.insensitive)
            }
            g.putString(titleHorizontalPosition, 0, actualTitle)
        }

        return g.newTextGraphics(
            TerminalPosition(1, 1),
            drawableArea
                .withRelativeColumns(-kotlin.math.min(2, drawableArea.columns))
                ?.withRelativeRows(-kotlin.math.min(2, drawableArea.rows)),
        )
    }

    override fun getDecoratedSize(window: Window?, contentAreaSize: TerminalSize?): TerminalSize? {
        val w = window ?: return contentAreaSize
        val content = contentAreaSize ?: TerminalSize.ZERO
        val themeDefinition = w.theme?.getDefinition(DefaultWindowDecorationRenderer::class.java)
        val useTitlePadding = themeDefinition?.getBooleanProperty("TITLE_PADDING", false) == true

        val titleWidth = TerminalTextUtils.getColumnWidth(w.title)
        var minPadding = TITLE_POSITION_WITHOUT_PADDING * 2
        if (useTitlePadding) {
            minPadding = TITLE_POSITION_WITH_PADDING * 2
        }

        return content
            .withRelativeColumns(2)
            ?.withRelativeRows(2)
            ?.max(TerminalSize(titleWidth + minPadding, 1))
    }

    override fun getOffset(window: Window?): TerminalPosition {
        return OFFSET
    }

    companion object {
        private const val TITLE_POSITION_WITH_PADDING = 4
        private const val TITLE_POSITION_WITHOUT_PADDING = 3
        private val OFFSET = TerminalPosition(1, 1)
    }
}
