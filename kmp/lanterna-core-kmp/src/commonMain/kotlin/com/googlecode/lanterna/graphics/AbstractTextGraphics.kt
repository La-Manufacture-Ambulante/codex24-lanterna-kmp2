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
import com.googlecode.lanterna.screen.TabBehaviour

import java.util.Arrays
import java.util.EnumSet

/**
 * This class hold the default logic for drawing the basic text graphic as exposed by TextGraphic. All implementations
 * rely on a setCharacter method being implemented in subclasses.
 * @author Martin
 */
abstract class AbstractTextGraphics protected constructor():TextGraphics {
protected var foregroundColor:TextColor? = null
protected var backgroundColor:TextColor? = null
protected var tabBehaviour:TabBehaviour? = null
@get:Override
 val activeModifiers:EnumSet<SGR?>?
private val shapeRenderer:ShapeRenderer?

/**
 * Returns screen coordinates of top-left corner of this TextGraphics.
 * The default implementation returns [TerminalPosition.TOP_LEFT_CORNER].
 * Subclasses that offset the graphics must override this method.
 * 
 * @return screen coordinates of top-left corner.
 */
    protected val screenLocation:TerminalPosition
get() {
return TerminalPosition.TOP_LEFT_CORNER
}

init{
this.activeModifiers = EnumSet.noneOf(SGR::class.java)
this.tabBehaviour = TabBehaviour.ALIGN_TO_COLUMN_4
this.foregroundColor = TextColor.ANSI.DEFAULT
this.backgroundColor = TextColor.ANSI.DEFAULT
this.shapeRenderer = DefaultShapeRenderer(???({ this.setCharacter() }))
}

@Override
 fun getBackgroundColor():TextColor? {
return backgroundColor
}

@Override
 fun setBackgroundColor(backgroundColor:TextColor?):TextGraphics? {
this.backgroundColor = backgroundColor
return this
}

@Override
 fun getForegroundColor():TextColor? {
return foregroundColor
}

@Override
 fun setForegroundColor(foregroundColor:TextColor?):TextGraphics? {
this.foregroundColor = foregroundColor
return this
}

@Override
 fun enableModifiers(vararg modifiers:SGR?):TextGraphics? {
enableModifiers(Arrays.asList(modifiers))
return this
}

private fun enableModifiers(modifiers:Collection<SGR?>?) {
this.activeModifiers!!.addAll(modifiers)
}

@Override
 fun disableModifiers(vararg modifiers:SGR?):TextGraphics? {
disableModifiers(Arrays.asList(modifiers))
return this
}

private fun disableModifiers(modifiers:Collection<SGR?>?) {
this.activeModifiers!!.removeAll(modifiers)
}

@Override
@Synchronized  fun setModifiers(modifiers:EnumSet<SGR?>?):TextGraphics? {
activeModifiers!!.clear()
activeModifiers!!.addAll(modifiers)
return this
}

@Override
 fun clearModifiers():TextGraphics? {
this.activeModifiers!!.clear()
return this
}

@Override
 fun getTabBehaviour():TabBehaviour? {
return tabBehaviour
}

@Override
 fun setTabBehaviour(tabBehaviour:TabBehaviour?):TextGraphics? {
if (tabBehaviour != null)
{
this.tabBehaviour = tabBehaviour
}
return this
}

@Override
 fun fill(c:Char):TextGraphics? {
fillRectangle(TerminalPosition.TOP_LEFT_CORNER, getSize(), c)
return this
}

@Override
 fun setCharacter(column:Int, row:Int, character:Char):TextGraphics? {
return setCharacter(column, row, newTextCharacter(character).toChar())
}

@Override
 fun setCharacter(position:TerminalPosition, textCharacter:TextCharacter?):TextGraphics? {
setCharacter(position.column, position.row, textCharacter!!.toChar())
return this
}

@Override
 fun setCharacter(position:TerminalPosition, character:Char):TextGraphics? {
return setCharacter(position.column, position.row, character)
}

@Override
 fun drawLine(fromPosition:TerminalPosition?, toPoint:TerminalPosition?, character:Char):TextGraphics? {
return drawLine(fromPosition, toPoint, newTextCharacter(character))
}

@Override
 fun drawLine(fromPoint:TerminalPosition?, toPoint:TerminalPosition?, character:TextCharacter?):TextGraphics? {
shapeRenderer!!.drawLine(fromPoint, toPoint, character)
return this
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:Char):TextGraphics? {
return drawLine(fromX, fromY, toX, toY, newTextCharacter(character))
}

@Override
 fun drawLine(fromX:Int, fromY:Int, toX:Int, toY:Int, character:TextCharacter?):TextGraphics? {
return drawLine(TerminalPosition(fromX, fromY), TerminalPosition(toX, toY), character)
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGraphics? {
return drawTriangle(p1, p2, p3, newTextCharacter(character))
}

@Override
 fun drawTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGraphics? {
shapeRenderer!!.drawTriangle(p1, p2, p3, character)
return this
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:Char):TextGraphics? {
return fillTriangle(p1, p2, p3, newTextCharacter(character))
}

@Override
 fun fillTriangle(p1:TerminalPosition?, p2:TerminalPosition?, p3:TerminalPosition?, character:TextCharacter?):TextGraphics? {
shapeRenderer!!.fillTriangle(p1, p2, p3, character)
return this
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGraphics? {
return drawRectangle(topLeft, size, newTextCharacter(character))
}

@Override
 fun drawRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGraphics? {
shapeRenderer!!.drawRectangle(topLeft, size, character)
return this
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:Char):TextGraphics? {
return fillRectangle(topLeft, size, newTextCharacter(character))
}

@Override
 fun fillRectangle(topLeft:TerminalPosition?, size:TerminalSize?, character:TextCharacter?):TextGraphics? {
shapeRenderer!!.fillRectangle(topLeft, size, character)
return this
}

@Override
@JvmOverloads  fun drawImage(
topLeft:TerminalPosition?, 
image:TextImage?, 
sourceImageTopLeft:TerminalPosition = TerminalPosition.TOP_LEFT_CORNER, 
sourceImageSize:TerminalSize? = image!!.getSize()):TextGraphics? {
var topLeft = topLeft
var sourceImageTopLeft = sourceImageTopLeft
var sourceImageSize = sourceImageSize

 // If the source image position is negative, offset the whole image
        if (sourceImageTopLeft.column < 0)
{
topLeft = topLeft!!.withRelativeColumn(-sourceImageTopLeft.column)
sourceImageSize = sourceImageSize!!.withRelativeColumns(sourceImageTopLeft.column)
sourceImageTopLeft = sourceImageTopLeft.withColumn(0)
}
if (sourceImageTopLeft.row < 0)
{
topLeft = topLeft!!.withRelativeRow(-sourceImageTopLeft.row)
sourceImageSize = sourceImageSize!!.withRelativeRows(sourceImageTopLeft.row)
sourceImageTopLeft = sourceImageTopLeft.withRow(0)
}

 // cropping specified image-subrectangle to the image itself:
        var fromRow = Math.max(sourceImageTopLeft.row, 0)
var untilRow = Math.min(sourceImageTopLeft.row + sourceImageSize!!.rows, image!!.getSize().getRows())
var fromColumn = Math.max(sourceImageTopLeft.column, 0)
var untilColumn = Math.min(sourceImageTopLeft.column + sourceImageSize!!.columns, image!!.getSize().getColumns())

 // difference between position in image and position on target:
        val diffRow = topLeft!!.row - sourceImageTopLeft.row
val diffColumn = topLeft!!.column - sourceImageTopLeft.column

 // top/left-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        fromRow = Math.max(fromRow, -diffRow)
fromColumn = Math.max(fromColumn, -diffColumn)

 // bot/right-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        untilRow = Math.min(untilRow, getSize().getRows() - diffRow)
untilColumn = Math.min(untilColumn, getSize().getColumns() - diffColumn)

if (fromRow >= untilRow || fromColumn >= untilColumn)
{
return this
}
for (row in fromRow until untilRow)
{
for (column in fromColumn until untilColumn)
{
setCharacter(column + diffColumn, row + diffRow, image!!.getCharacterAt(column, row))
}
}
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?):TextGraphics? {
var string = string
string = prepareStringForPut(column, string!!)
var offset = 0
for (i in 0 until string!!.length())
{
val character = string!!.charAt(i)
setCharacter(column + offset, row, newTextCharacter(character).toChar())
offset += getOffsetToNextCharacter(character)
}
return this
}

@Override
 fun putString(position:TerminalPosition, string:String?):TextGraphics? {
putString(position.column, position.row, string)
return this
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGraphics? {
clearModifiers()
return putString(column, row, string, EnumSet.of(extraModifier, optionalExtraModifiers))
}

@Override
 fun putString(column:Int, row:Int, string:String?, extraModifiers:Collection<SGR?>?):TextGraphics? {
val newModifiers = EnumSet.copyOf(extraModifiers)
newModifiers!!.removeAll(activeModifiers)
enableModifiers(newModifiers)
putString(column, row, string)
disableModifiers(newModifiers)
return this
}

@Override
 fun putString(position:TerminalPosition, string:String?, extraModifier:SGR?, vararg optionalExtraModifiers:SGR?):TextGraphics? {
putString(position.column, position.row, string, extraModifier, *optionalExtraModifiers)
return this
}

@Override
@Synchronized  fun putCSIStyledString(column:Int, row:Int, string:String?):TextGraphics? {
var string = string
val original = StyleSet.Set(this)
string = prepareStringForPut(column, string!!)
var offset = 0
var i = 0
while (i < string!!.length())
{
val character = string!!.charAt(i)
val controlSequence = TerminalTextUtils.getANSIControlSequenceAt(string, i)
if (controlSequence != null)
{
TerminalTextUtils.updateModifiersFromCSICode(controlSequence, this, original)

 // Skip the control sequence, leaving one extra, since we'll add it when we loop
                i += controlSequence!!.length() - 1
i++
continue
}

setCharacter(column + offset, row, newTextCharacter(character).toChar())
offset += getOffsetToNextCharacter(character)
i++
}

setStyleFrom(original)
return this
}

@Override
 fun putCSIStyledString(position:TerminalPosition, string:String?):TextGraphics? {
return putCSIStyledString(position.column, position.row, string)
}

@Override
 fun getCharacter(position:TerminalPosition):TextCharacter? {
return getCharacter(position.column, position.row)
}

@Override
 fun toScreenPosition(pos:TerminalPosition?):TerminalPosition? {
val max = screenLocation.plus(
TerminalPosition(getSize().getColumns() - 1, getSize().getRows() - 1))
val loc = screenLocation.plus(pos)
if (loc!!.column > max!!.column || loc!!.row > max!!.row)
{
return null
}
else
{
return loc
}
}

@Override
@Throws(IllegalArgumentException::class)
 fun newTextGraphics(topLeftCorner:TerminalPosition, size:TerminalSize):TextGraphics? {
val writableArea = getSize()
if ((topLeftCorner.column + size.columns <= 0 || 
topLeftCorner.column >= writableArea!!.columns || 
topLeftCorner.row + size.rows <= 0 || 
topLeftCorner.row >= writableArea!!.rows))
{
 //The area selected is completely outside of this TextGraphics, so we can return a "null" object that doesn't
            //do anything because it is impossible to change anything anyway
            return NullTextGraphics(size)
}
return SubTextGraphics(this, topLeftCorner, screenLocation, size)
}

private fun newTextCharacter(character:Char):TextCharacter {
return TextCharacter(character, foregroundColor, backgroundColor, activeModifiers)
}

private fun prepareStringForPut(column:Int, string:String):String? {
var string = string
if (string.contains("\n"))
{
string = string.substring(0, string.indexOf("\n"))
}
if (string.contains("\r"))
{
string = string.substring(0, string.indexOf("\r"))
}
string = tabBehaviour!!.replaceTabs(string, column)
return string
}

private fun getOffsetToNextCharacter(character:Char):Int {
if (TerminalTextUtils.isCharDoubleWidth(character))
{
 //CJK characters are twice the normal characters in width, so next character position is two columns forward
            return 2
}
else
{
 //For "normal" characters we advance to the next column
            return 1
}
}

@Override
 fun setStyleFrom(source:StyleSet<*>):TextGraphics? {
setBackgroundColor(source.getBackgroundColor())
setForegroundColor(source.getForegroundColor())
setModifiers(source.getActiveModifiers())
return this
}

}
