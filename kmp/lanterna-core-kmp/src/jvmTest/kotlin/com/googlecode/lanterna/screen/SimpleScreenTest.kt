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
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object SimpleScreenTest {

private val COLORS_TO_CYCLE = arrayOf<TextColor?>(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE, TextColor.ANSI.CYAN, TextColor.ANSI.GREEN, TextColor.ANSI.MAGENTA, TextColor.ANSI.RED, TextColor.ANSI.YELLOW)

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = TestTerminalFactory(args).createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()
screen.refresh()

val textGraphics = screen.newTextGraphics()

var foregroundCycle = 1
var backgroundCycle = 0

mainLoop@ while (true)
{
val keyStroke = screen.readInput()
when (keyStroke!!.keyType) {
KeyType.EOF, KeyType.ESCAPE -> break@mainLoop

KeyType.ARROW_UP -> screen.cursorPosition = screen.cursorPosition!!.withRelativeRow(-1)

KeyType.ARROW_DOWN -> screen.cursorPosition = screen.cursorPosition!!.withRelativeRow(1)

KeyType.ARROW_LEFT -> screen.cursorPosition = screen.cursorPosition!!.withRelativeColumn(-1)

KeyType.ARROW_RIGHT -> screen.cursorPosition = screen.cursorPosition!!.withRelativeColumn(1)

KeyType.CHARACTER -> if (keyStroke!!.isCtrlDown())
{
when (keyStroke.character) {
'k' -> {
screen.setCharacter(screen.cursorPosition, TextCharacter('桜', COLORS_TO_CYCLE[foregroundCycle], COLORS_TO_CYCLE[backgroundCycle]))
screen.cursorPosition = screen.cursorPosition!!.withRelativeColumn(2)
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
	else -> {}
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
textGraphics!!.putString(0, (screen.terminalSize?.rows ?: 2) - 2, "Foreground color")

if (COLORS_TO_CYCLE[backgroundCycle] !== TextColor.ANSI.BLACK)
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.BLACK)
}
else
{
textGraphics!!.setBackgroundColor(TextColor.ANSI.WHITE)
}
textGraphics!!.setForegroundColor(COLORS_TO_CYCLE[backgroundCycle])
textGraphics!!.putString(0, (screen.terminalSize?.rows ?: 1) - 1, "Background color")
}
else
{
val ch = keyStroke.character ?: continue
screen.setCharacter(screen.cursorPosition, TextCharacter(ch, COLORS_TO_CYCLE[foregroundCycle], COLORS_TO_CYCLE[backgroundCycle]))
screen.cursorPosition = screen.cursorPosition!!.withRelativeColumn(1)
break
}
else -> {}
}

screen.refresh()
}

screen.stopScreen()
}
}
