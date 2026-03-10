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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna

import java.util.Objects

/**
 * This class is immutable and cannot change its internal state after creation.
 * 
 * @author ginkoblongata
 */
 class TerminalRectangle/**
 * Creates a new terminal rect representation at the supplied x y position with the supplied width and height.
 * 
 * Both width and height must be at least zero (non negative) as checked in TerminalSize.
 * 
 * @param width number of columns
 * @param height number of rows
 */
    ( val x:Int,  val y:Int, /**
 * @return Returns the width of this rect, in number of columns
 */
     val columns:Int, /**
 * @return Returns the height of this rect representation, in number of rows
 */
     val rows:Int) {

 // one of the benefits of immutable: ease of usage
     val position:TerminalPosition?
 val size:TerminalSize?

 val xAndWidth:Int
 val yAndHeight:Int

init{
position = TerminalPosition(x, y)
size = TerminalSize(columns, rows)
this.xAndWidth = x + columns
this.yAndHeight = y + rows
}

/**
 * Creates a new rect based on this rect, but with a different width
 * @param columns Width of the new rect, in columns
 * @return New rect based on this one, but with a new width
 */
     fun withColumns(columns:Int):TerminalRectangle {
return TerminalRectangle(x, y, columns, rows)
}

/**
 * Creates a new rect based on this rect, but with a different height
 * @param rows Height of the new rect, in rows
 * @return New rect based on this one, but with a new height
 */
     fun withRows(rows:Int):TerminalRectangle {
return TerminalRectangle(x, y, columns, rows)
}

 fun whenContains(p:TerminalPosition, op:Runnable?):Boolean {
return whenContains(p.column, p.row, op)
}
 fun whenContains(x:Int, y:Int, op:Runnable?):Boolean {
if (this.x <= x && x < this.xAndWidth && this.y <= y && y < this.yAndHeight)
{
op!!.run()
return true
}
return false
}


 override fun toString():String {
return "{x: " + x + ", y: " + y + ", width: " + columns + ", height: " + rows + "}"
}

 override fun equals(obj:Any?):Boolean {
return (obj != null
&& this::class == obj::class
&& Objects.equals(position, (obj as TerminalRectangle).position)
&& Objects.equals(size, (obj as TerminalRectangle).size))
}

 override fun hashCode():Int {
return Objects.hash(position, size)
}
}
