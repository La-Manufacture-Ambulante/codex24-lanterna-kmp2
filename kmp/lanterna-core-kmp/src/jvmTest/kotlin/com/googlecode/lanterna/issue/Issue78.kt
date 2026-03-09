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
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.Terminal
import java.io.IOException

/**
 * 
 * @author martin
 */
 object Issue78 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val t = TestTerminalFactory(args).createTerminal()
t!!.enterPrivateMode()
val s = TerminalScreen(t)
s.startScreen()
try
{
Thread.sleep(1000)
}
catch (e:InterruptedException) {}

s.stopScreen()
t!!.exitPrivateMode()
}
}
