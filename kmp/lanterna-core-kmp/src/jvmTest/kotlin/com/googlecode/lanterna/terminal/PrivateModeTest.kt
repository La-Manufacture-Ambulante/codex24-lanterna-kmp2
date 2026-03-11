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
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType

import java.awt.*
import java.io.IOException

/**
 * 
 * @author martin
 */
 object PrivateModeTest {

@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
val terminal = TestTerminalFactory(args)
.setTerminalEmulatorFrameAutoCloseTrigger(null)
.createTerminal() as Terminal
var normalTerminal = true
printNormalTerminalText(terminal!!)
var keyStroke:KeyStroke? = null
while (keyStroke == null || keyStroke!!.getKeyType() != KeyType.ESCAPE)
{
keyStroke = terminal!!.pollInput()
if (keyStroke != null && keyStroke!!.getKeyType() == KeyType.CHARACTER && keyStroke!!.getCharacter() == ' ')
{
normalTerminal = !normalTerminal
if (normalTerminal)
{
terminal!!.exitPrivateMode()
printNormalTerminalText(terminal!!)
}
else
{
terminal!!.enterPrivateMode()
printPrivateModeTerminalText(terminal!!)
}
}
else
{
Thread.sleep(1)
}
}
if (!normalTerminal)
{
terminal!!.exitPrivateMode()
}
terminal!!.putCharacter('\n')
if (terminal is Window)
{
(terminal as Window).dispose()
}
}

@Throws(IOException::class)
private fun printNormalTerminalText(terminal:Terminal) {
terminal.clearScreen()
terminal.setCursorPosition(5, 3)
val text = "Normal terminal, press space to switch"
for (i in 0 until text.length)
{
terminal.putCharacter(text.charAt(i))
}
terminal.flush()
}

@Throws(IOException::class)
private fun printPrivateModeTerminalText(terminal:Terminal) {
terminal.clearScreen()
terminal.setCursorPosition(5, 3)
val text = "Private mode terminal, press space to switch"
for (i in 0 until text.length)
{
terminal.putCharacter(text.charAt(i))
}
terminal.flush()
}
}
