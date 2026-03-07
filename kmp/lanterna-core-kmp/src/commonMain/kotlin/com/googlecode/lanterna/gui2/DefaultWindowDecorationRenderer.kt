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

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * Default window decoration renderer that is used unless overridden with another decoration renderer. The windows are
 * drawn using a bevel colored line and the window title in the top-left corner, very similar to ordinary titled
 * borders.
 * 
 * @author Martin
 */
 class DefaultWindowDecorationRenderer:WindowDecorationRenderer {

@Override
 fun draw(textGUI:WindowBasedTextGUI?, graphics:TextGUIGraphics?, window:Window):TextGUIGraphics? {
var title = window.getTitle()
if (title == null)
{
title = ""
}

val drawableArea = graphics!!.getSize()
val themeDefinition = window.getTheme().getDefinition(DefaultWindowDecorationRenderer::class.java)
val horizontalLine = themeDefinition!!.getCharacter("HORIZONTAL_LINE", Symbols.SINGLE_LINE_HORIZONTAL)
val verticalLine = themeDefinition!!.getCharacter("VERTICAL_LINE", Symbols.SINGLE_LINE_VERTICAL)
val bottomLeftCorner = themeDefinition!!.getCharacter("BOTTOM_LEFT_CORNER", Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)
val topLeftCorner = themeDefinition!!.getCharacter("TOP_LEFT_CORNER", Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
val bottomRightCorner = themeDefinition!!.getCharacter("BOTTOM_RIGHT_CORNER", Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
val topRightCorner = themeDefinition!!.getCharacter("TOP_RIGHT_CORNER", Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
val titleSeparatorLeft = themeDefinition!!.getCharacter("TITLE_SEPARATOR_LEFT", Symbols.SINGLE_LINE_HORIZONTAL)
val titleSeparatorRight = themeDefinition!!.getCharacter("TITLE_SEPARATOR_RIGHT", Symbols.SINGLE_LINE_HORIZONTAL)
val useTitlePadding = themeDefinition!!.getBooleanProperty("TITLE_PADDING", false)
val centerTitle = themeDefinition!!.getBooleanProperty("CENTER_TITLE", false)

var titleHorizontalPosition = if (useTitlePadding) TITLE_POSITION_WITH_PADDING else TITLE_POSITION_WITHOUT_PADDING
val titleMaxColumns = drawableArea!!.columns - titleHorizontalPosition * 2
if (centerTitle)
{
titleHorizontalPosition = (drawableArea!!.columns / 2) - (TerminalTextUtils.getColumnWidth(title) / 2)
titleHorizontalPosition = Math.max(titleHorizontalPosition, if (useTitlePadding) TITLE_POSITION_WITH_PADDING else TITLE_POSITION_WITHOUT_PADDING)
}
val actualTitle = TerminalTextUtils.fitString(title, titleMaxColumns)
val titleActualColumns = TerminalTextUtils.getColumnWidth(actualTitle)

 // Don't draw highlights on menu popup windows
        if (window.getHints().contains(Window.Hint.MENU_POPUP))
{
graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getPreLight())
}
graphics!!.drawLine(TerminalPosition(0, drawableArea!!.rows - 2), TerminalPosition(0, 1), verticalLine)
graphics!!.drawLine(TerminalPosition(1, 0), TerminalPosition(drawableArea!!.columns - 2, 0), horizontalLine)
graphics!!.setCharacter(0, 0, topLeftCorner)
graphics!!.setCharacter(0, drawableArea!!.rows - 1, bottomLeftCorner)

if (!actualTitle!!.isEmpty() && drawableArea!!.columns > 8)
{
var separatorOffset = 1
if (useTitlePadding)
{
graphics!!.setCharacter(titleHorizontalPosition - 1, 0, ' ')
graphics!!.setCharacter(titleHorizontalPosition + titleActualColumns, 0, ' ')
separatorOffset = 2
}
graphics!!.setCharacter(titleHorizontalPosition - separatorOffset, 0, titleSeparatorLeft)
graphics!!.setCharacter(titleHorizontalPosition + titleActualColumns + separatorOffset - 1, 0, titleSeparatorRight)
}

graphics!!.applyThemeStyle(themeDefinition!!.getNormal())
graphics!!.drawLine(
TerminalPosition(drawableArea!!.columns - 1, 1), 
TerminalPosition(drawableArea!!.columns - 1, drawableArea!!.rows - 2), 
verticalLine)
graphics!!.drawLine(
TerminalPosition(1, drawableArea!!.rows - 1), 
TerminalPosition(drawableArea!!.columns - 2, drawableArea!!.rows - 1), 
horizontalLine)

graphics!!.setCharacter(drawableArea!!.columns - 1, 0, topRightCorner)
graphics!!.setCharacter(drawableArea!!.columns - 1, drawableArea!!.rows - 1, bottomRightCorner)

if (!actualTitle!!.isEmpty())
{
if (textGUI!!.getActiveWindow() === window)
{
graphics!!.applyThemeStyle(themeDefinition!!.getActive())
}
else
{
graphics!!.applyThemeStyle(themeDefinition!!.getInsensitive())
}
graphics!!.putString(titleHorizontalPosition, 0, actualTitle)
}

return graphics!!.newTextGraphics(
TerminalPosition(1, 1), 
drawableArea!!
 // Make sure we don't make the new graphic's area smaller than 0
                        .withRelativeColumns(-(Math.min(2, drawableArea!!.columns)))!!
.withRelativeRows(-(Math.min(2, drawableArea!!.rows))))
}

@Override
 fun getDecoratedSize(window:Window, contentAreaSize:TerminalSize?):TerminalSize? {
val themeDefinition = window.getTheme().getDefinition(DefaultWindowDecorationRenderer::class.java)
val useTitlePadding = themeDefinition!!.getBooleanProperty("TITLE_PADDING", false)

val titleWidth = TerminalTextUtils.getColumnWidth(window.getTitle())
var minPadding = TITLE_POSITION_WITHOUT_PADDING * 2
if (useTitlePadding)
{
minPadding = TITLE_POSITION_WITH_PADDING * 2
}

return contentAreaSize!!
.withRelativeColumns(2)!!
.withRelativeRows(2)!!
.max(TerminalSize(titleWidth + minPadding, 1))  //Make sure the title fits!
}

@Override
 fun getOffset(window:Window?):TerminalPosition {
return OFFSET
}

companion object {

private val TITLE_POSITION_WITH_PADDING = 4
private val TITLE_POSITION_WITHOUT_PADDING = 3

private val OFFSET = TerminalPosition(1, 1)
}
}
