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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.BasicTextImage
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.graphics.TextImage

/**
 * Defines a buffer used by AbstractScreen and its subclasses to keep its state of what's currently displayed and what
 * the edit buffer looks like. A ScreenBuffer is essentially a two-dimensional array of TextCharacter with some utility
 * methods to inspect and manipulate it in a safe way.
 * @author martin
 */
 class ScreenBuffer private constructor(private val backend:BasicTextImage?):TextImage {

/**//////////////////////////////////////////////////////////////////////////// */
    //  Delegate all TextImage calls (except resize) to the backend BasicTextImage
     val size:TerminalSize?
@Override
get() {
return backend!!.getSize()
}

/**
 * Creates a new ScreenBuffer with a given size and a TextCharacter to initially fill it with
 * @param size Size of the buffer
 * @param filler What character to set as the initial content of the buffer
 */
     constructor(size:TerminalSize?, filler:TextCharacter?) : this(BasicTextImage(size, filler)) {}

@Override
 fun resize(newSize:TerminalSize?, filler:TextCharacter?):ScreenBuffer {
val resizedBackend = backend!!.resize(newSize, filler)
return ScreenBuffer(resizedBackend)
}

internal fun isVeryDifferent(other:ScreenBuffer, threshold:Int):Boolean {
if (!size!!.equals(other.size))
{
throw IllegalArgumentException(("Can only call isVeryDifferent comparing two ScreenBuffers of the same size!" + " This is probably a bug in Lanterna."))
}
var differences = 0
for (y in 0 until size!!.rows)
{
for (x in 0 until size!!.columns)
{
if (!getCharacterAt(x, y)!!.equals(other.getCharacterAt(x, y)))
{
if (++differences >= threshold)
{
return true
}
}
}
}
return false
}

@Override
 fun getCharacterAt(position:TerminalPosition?):TextCharacter? {
return backend!!.getCharacterAt(position)
}

@Override
 fun getCharacterAt(column:Int, row:Int):TextCharacter? {
return backend!!.getCharacterAt(column, row)
}

@Override
 fun setCharacterAt(position:TerminalPosition?, character:TextCharacter?) {
backend!!.setCharacterAt(position, character)
}

@Override
 fun setCharacterAt(column:Int, row:Int, character:TextCharacter?) {
backend!!.setCharacterAt(column, row, character)
}

@Override
 fun setAll(character:TextCharacter?) {
backend!!.setAll(character)
}

@Override
 fun newTextGraphics():TextGraphics? {
return backend!!.newTextGraphics()
}

@Override
 fun copyTo(destination:TextImage?) {
var destination = destination
if (destination is ScreenBuffer)
{
 //This will allow the BasicTextImage's copy method to use System.arraycopy (micro-optimization?)
            destination = (destination as ScreenBuffer).backend
}
backend!!.copyTo(destination)
}

@Override
 fun copyTo(destination:TextImage?, startRowIndex:Int, rows:Int, startColumnIndex:Int, columns:Int, destinationRowOffset:Int, destinationColumnOffset:Int) {
var destination = destination
if (destination is ScreenBuffer)
{
 //This will allow the BasicTextImage's copy method to use System.arraycopy (micro-optimization?)
            destination = (destination as ScreenBuffer).backend
}
backend!!.copyTo(destination, startRowIndex, rows, startColumnIndex, columns, destinationRowOffset, destinationColumnOffset)
}

/**
 * Copies the content from a TextImage into this buffer.
 * @param source Source to copy content from
 * @param startRowIndex Which row in the source image to start copying from
 * @param rows How many rows to copy
 * @param startColumnIndex Which column in the source image to start copying from
 * @param columns How many columns to copy
 * @param destinationRowOffset The row offset in this buffer of where to place the copied content
 * @param destinationColumnOffset The column offset in this buffer of where to place the copied content
 */
     fun copyFrom(source:TextImage, startRowIndex:Int, rows:Int, startColumnIndex:Int, columns:Int, destinationRowOffset:Int, destinationColumnOffset:Int) {
source.copyTo(backend, startRowIndex, rows, startColumnIndex, columns, destinationRowOffset, destinationColumnOffset)
}

@Override
 fun scrollLines(firstLine:Int, lastLine:Int, distance:Int) {
backend!!.scrollLines(firstLine, lastLine, distance)
}

@Override
 fun toString():String? {
return backend!!.toString()
}
}
