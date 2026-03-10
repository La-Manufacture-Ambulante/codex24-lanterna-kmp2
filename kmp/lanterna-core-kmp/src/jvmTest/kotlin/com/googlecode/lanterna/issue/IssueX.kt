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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.screen.Screen
import java.io.IOException

internal object IssueX {

@Throws(InterruptedException::class, IOException::class)
 fun main(args:Array<String?>?) {
val writer = LanternaTerminalWriter(args)
for (i in 0..999)
{
writer.write(String.valueOf(i), SGR.BOLD)
Thread.sleep(100)
}
writer.close()
}

 class LanternaTerminalWriter @Throws(IOException::class)
 constructor(args:Array<String?>?) {

private val screen:Screen?
private val screenWriter:TextGraphics?

init{
screen = TestTerminalFactory(args).createScreen()
screen!!.startScreen()

screenWriter = screen!!.newTextGraphics()
}

@Throws(IOException::class)
 fun close() {
screen!!.stopScreen()
}

@Throws(IOException::class)
 fun write(string:String?, vararg styles:SGR?) {
screenWriter!!.enableModifiers(styles)
val current_y = 1
val default_x = 3
screenWriter!!.putString(default_x, current_y, string)
screen!!.pollInput()
screen!!.refresh()
}

}
}

