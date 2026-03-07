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
import com.googlecode.lanterna.graphics.ThemedTextGraphics

/**
 * This WindowPostRenderer implementation draws a shadow under the window
 * 
 * @author Martin
 */
 class WindowShadowRenderer:WindowPostRenderer {
@Override
 fun postRender(
textGraphics:ThemedTextGraphics, 
textGUI:TextGUI?, 
window:Window) {

val windowPosition = window.getPosition()
val decoratedWindowSize = window.getDecoratedSize()
val themeDefinition = window.getTheme().getDefinition(WindowShadowRenderer::class.java)
textGraphics.applyThemeStyle(themeDefinition!!.getNormal())
val filler = themeDefinition!!.getCharacter("FILLER", ' ')
val useDoubleWidth = themeDefinition!!.getBooleanProperty("DOUBLE_WIDTH", true)
val useTransparency = themeDefinition!!.getBooleanProperty("TRANSPARENT", false)

val lowerLeft = windowPosition!!.withRelativeColumn(if (useDoubleWidth) 2 else 1)!!.withRelativeRow(decoratedWindowSize!!.rows)
var lowerRight = lowerLeft!!.withRelativeColumn(decoratedWindowSize!!.columns - (if (useDoubleWidth) 3 else 2))
var column = lowerLeft!!.column
while (column <= lowerRight!!.column + 1)
{
var characterToDraw = filler
if (useTransparency)
{
val tc = textGraphics.getCharacter(column, lowerLeft!!.row)
if (tc != null)
{
characterToDraw = tc!!.getCharacterString().charAt(0)
}
}
textGraphics.setCharacter(column, lowerLeft!!.row, characterToDraw)
if (TerminalTextUtils.isCharDoubleWidth(characterToDraw))
{
column++
}
column++
}

lowerRight = lowerRight!!.withRelativeColumn(1)
var upperRight = lowerRight!!.withRelativeRow(-decoratedWindowSize!!.rows + 1)
var hasDoubleWidthShadow = false
for (row in upperRight!!.row until lowerRight!!.row)
{
var characterToDraw = filler
if (useTransparency)
{
val tc = textGraphics.getCharacter(upperRight!!.column, row)
if (tc != null)
{
characterToDraw = tc!!.getCharacterString().charAt(0)
}
}
textGraphics.setCharacter(upperRight!!.column, row, characterToDraw)
if (TerminalTextUtils.isCharDoubleWidth(characterToDraw))
{
hasDoubleWidthShadow = true
}
}

textGraphics.applyThemeStyle(themeDefinition!!.getNormal())
if (useDoubleWidth || hasDoubleWidthShadow)
{
 //Fill the remaining hole
            upperRight = upperRight!!.withRelativeColumn(1)
for (row in upperRight!!.row..lowerRight!!.row)
{
var characterToDraw = filler
if (useTransparency)
{
val tc = textGraphics.getCharacter(upperRight!!.column, row)
if (tc != null && !tc!!.isDoubleWidth())
{
characterToDraw = tc!!.getCharacterString().charAt(0)
}
}
val neighbour = textGraphics.getCharacter(upperRight!!.column - 1, row)
 // Only need to draw this is the character to the left isn't double-width
                if (neighbour != null && !neighbour!!.isDoubleWidth())
{
textGraphics.setCharacter(upperRight!!.column, row, characterToDraw)
}
}
}
}
}
