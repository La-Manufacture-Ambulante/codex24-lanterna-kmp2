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
package com.googlecode.lanterna.screen

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.graphics.DoublePrintingTextGraphics
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import java.io.IOException
import java.util.Random

/**
 * 
 * @author martin
 */
 object ScreenTriangleTest {

private val oneThirdOf2PI = (Math.PI * 2.0) / 3.0
private val twoThirdsOf2PI = oneThirdOf2PI * 2.0

@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>) {
var useAnsiColors = false
var useFilled = false
var slow = false
var rotating = false
var square = false
for (arg in args)
{
if (arg!!.equals("--ansi-colors"))
{
useAnsiColors = true
}
if (arg!!.equals("--filled"))
{
useFilled = true
}
if (arg!!.equals("--slow"))
{
slow = true
}
if (arg!!.equals("--rotating"))
{
rotating = true
}
if (arg!!.equals("--square"))
{
square = true
}
}
val screen = TestTerminalFactory(args).createScreen()
screen.startScreen()

var graphics:TextGraphics? = ScreenTextGraphics(screen)
if (square)
{
graphics = DoublePrintingTextGraphics(graphics!!)
}
val random = Random()

var color:TextColor? = null
var rad = 0.0
while (true)
{
val keyStroke = screen.pollInput()
if ((keyStroke != null && (keyStroke!!.keyType == KeyType.ESCAPE || keyStroke!!.keyType == KeyType.EOF)))
{
break
}
screen.doResizeIfNecessary()
val size = graphics!!.size
if (useAnsiColors)
{
if (color == null || !rotating)
{
color = TextColor.ANSI.values()!![random.nextInt(TextColor.ANSI.values()!!.size)]
}
}
else
{
if (color == null || !rotating)
{
 //Draw a rectangle in random indexed color
                    color = TextColor.Indexed(random.nextInt(256))
}
}

val p1:TerminalPosition?
val p2:TerminalPosition?
val p3:TerminalPosition?
if (rotating)
{
screen.clear()
val triangleSize = 15.0
val x0 = (size!!.columns / 2) + (Math.cos(rad) * triangleSize) as Int
val y0 = (size!!.rows / 2) + (Math.sin(rad) * triangleSize) as Int
val x1 = (size!!.columns / 2) + (Math.cos(rad + oneThirdOf2PI) * triangleSize) as Int
val y1 = (size!!.rows / 2) + (Math.sin(rad + oneThirdOf2PI) * triangleSize) as Int
val x2 = (size!!.columns / 2) + (Math.cos(rad + twoThirdsOf2PI) * triangleSize) as Int
val y2 = (size!!.rows / 2) + (Math.sin(rad + twoThirdsOf2PI) * triangleSize) as Int
p1 = TerminalPosition(x0, y0)
p2 = TerminalPosition(x1, y1)
p3 = TerminalPosition(x2, y2)
rad += Math.PI / 90.0
}
else
{
p1 = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
p2 = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
p3 = TerminalPosition(random.nextInt(size!!.columns), random.nextInt(size!!.rows))
}

graphics!!.setBackgroundColor(color)
if (useFilled)
{
graphics!!.fillTriangle(p1, p2, p3, ' ')
}
else
{
graphics!!.drawTriangle(p1, p2, p3, ' ')
}
screen.refresh(Screen.RefreshType.DELTA)
if (slow)
{
Thread.sleep(500)
}
}
screen.stopScreen()
}
}
