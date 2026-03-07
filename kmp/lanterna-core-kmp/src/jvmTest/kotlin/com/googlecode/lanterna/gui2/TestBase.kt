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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.screen.Screen

import java.io.IOException

/**
 * Some common code for the GUI tests to get a text system up and running on a separate thread
 * @author Martin
 */
abstract class TestBase {
@Throws(IOException::class, InterruptedException::class)
internal fun run(args:Array<String?>?) {
val screen = TestTerminalFactory(args).createScreen()
screen!!.startScreen()
val textGUI = createTextGUI(screen)
val theme = extractTheme(args!!)
if (theme != null)
{
textGUI!!.setTheme(LanternaThemes.getRegisteredTheme(theme))
}
textGUI!!.setBlockingIO(false)
textGUI!!.setEOFWhenNoWindows(true)

        textGUI!!.isEOFWhenNoWindows()   //No meaning, just to silence IntelliJ:s "is never used" alert

try
{
init(textGUI)
val guiThread = textGUI!!.getGUIThread() as AsynchronousTextGUIThread
guiThread!!.start()
afterGUIThreadStarted(textGUI)
guiThread!!.waitForStop()
}

finally
{
screen!!.stopScreen()
}
}

private fun extractTheme(args:Array<String?>):String? {
for (i in args.indices)
{
if (args[i].equals("--theme") && i + 1 < args.size)
{
return args[i + 1]
}
}
return null
}

protected fun createTextGUI(screen:Screen?):MultiWindowTextGUI? {
return MultiWindowTextGUI(SeparateTextGUIThread.Factory(), screen)
}

abstract fun init(textGUI:WindowBasedTextGUI?) 
 fun afterGUIThreadStarted(textGUI:WindowBasedTextGUI?) {
 // By default do nothing
    }
}
