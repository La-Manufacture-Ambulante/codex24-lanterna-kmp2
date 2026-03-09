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
import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object SimpleScreenTest {

private val COLORS_TO_CYCLE = arrayOf<TextColor?>(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE, TextColor.ANSI.CYAN, TextColor.ANSI.GREEN, TextColor.ANSI.MAGENTA, TextColor.ANSI.RED, TextColor.ANSI.YELLOW)

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = TestTerminalFactory(args).createTerminal()
val screen = TerminalScreen(terminal)
screen.startScreen()
screen.refresh()

val textGraphics = screen.newTextGraphics()

var foregroundCycle = 1
var backgroundCycle = 0

mainLoop@ while (true)
{
val keyStroke = screen.readInput()
when (keyStroke!!.getKeyType()) {
EOF, ESCAPE -> break@mainLoop

ARROW_UP -> screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(-1))

ARROW_DOWN -> screen.setCursorPosition(screen.getCursorPosition().withRelativeRow(1))

ARROW_LEFT -> screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(-1))

ARROW_RIGHT -> screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(1))

CHARACTER -> if (keyStroke!!.isCtrlDown())
{
when (keyStroke!!.getCharacter()) {
'k' -> {
screen.setCharacter(screen.getCursorPosition(), TextCharacter('桜', COLORS_TO_CYCLE[foregroundCycle], COLORS_TO_CYCLE[backgroundCycle]))
screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(2))
}

'f' -> {
foregroundCycle++
if (foregroundCycle >= COLORS_TO_CYCLE.size)
{
foregroundCycle = 0
}
}

'b' -> {
backgroundCycle++
if (backgroundCycle >= COLORS_TO_CYCLE.size)
{
backgroundCycle = 0
}
}
}
if (COLORS_TO_CYCLE[foregroundCycle] !== TextColor.ANSI.BLACK)
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
}
else
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.WHITE)
}
textGraphics!!.setForegroundColor(COLORS_TO_CYCLE[foregroundCycle])
textGraphics!!.putString(0, screen.getTerminalSize().getRows() - 2, "Foreground color")

if (COLORS_TO_CYCLE[backgroundCycle] !== TextColor.ANSI.BLACK)
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
}
else
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.WHITE)
}
textGraphics!!.setForegroundColor(COLORS_TO_CYCLE[backgroundCycle])
textGraphics!!.putString(0, screen.getTerminalSize().getRows() - 1, "Background color")
}
else
{
screen.setCharacter(screen.getCursorPosition(), TextCharacter(keyStroke!!.getCharacter(), COLORS_TO_CYCLE[foregroundCycle], COLORS_TO_CYCLE[backgroundCycle]))
screen.setCursorPosition(screen.getCursorPosition().withRelativeColumn(1))
break
}
}

screen.refresh()
}

screen.stopScreen()
}
}
