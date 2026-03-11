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

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.screen.Screen

import java.io.IOException

 object MultiButtonTest {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val screen = TestTerminalFactory(args).createScreen()
screen!!.startScreen()
val textGUI = MultiWindowTextGUI(screen)
textGUI.setEOFWhenNoWindows(true)
try
{
val window = BasicWindow("Button test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(Button(""))
contentArea.addComponent(Button("TRE"))
contentArea.addComponent(Button("Button"))
contentArea.addComponent(Button("Another button"))
contentArea.addComponent(EmptySpace(TerminalSize(5, 1)))
 //contentArea.addComponent(new Button("Here is a\nmulti-line\ntext segment that is using \\n"));
            contentArea.addComponent(Button("OK", Runnable { window.close() }))

window.setComponent(contentArea)
textGUI.addWindowAndWait(window)
}

finally
{
screen!!.stopScreen()
}
}
}
