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
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.TestTerminalFactory
import java.io.IOException

/**
 * 
 * @author Martin
 */
 class TerminalResizeTest:TerminalResizeListener {

public override fun onResized(terminal:Terminal?, newSize:TerminalSize?) {
try
{
terminal!!.setCursorPosition(0, 0)
val string = "${newSize!!.columns}x${newSize.rows}                     "
val chars = string.toCharArray()
for (c in chars)
{
terminal.putCharacter(c)
}
terminal.flush()
}
catch (e:IOException) {
throw RuntimeException(e)
}

}

companion object {

@Throws(InterruptedException::class, IOException::class)
 fun main(args:Array<String?>?) {
val terminal = TestTerminalFactory(args).createTerminal()
terminal!!.enterPrivateMode()
terminal!!.clearScreen()
terminal!!.setCursorPosition(10, 5)
terminal!!.putCharacter('H')
terminal!!.putCharacter('e')
terminal!!.putCharacter('l')
terminal!!.putCharacter('l')
terminal!!.putCharacter('o')
terminal!!.putCharacter('!')
terminal!!.setCursorPosition(0, 0)
terminal!!.flush()
terminal!!.addResizeListener(TerminalResizeTest())

while (true)
{
val key = terminal!!.pollInput()
if (key == null || key!!.getCharacter() != 'q')
{
Thread.sleep(1)
}
else
{
break
}
}
terminal!!.exitPrivateMode()
}
}
}
