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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.screen.Screen

import java.io.IOException

 object MultiLabelTest {
@Throws(IOException::class, InterruptedException::class)
 fun main(args:Array<String?>?) {
val screen = TestTerminalFactory(args).createScreen()
screen!!.startScreen()
val textGUI = MultiWindowTextGUI(screen)
try
{
val window = BasicWindow("Label test")
val contentArea = Panel()
contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))
contentArea.addComponent(Label("This is a single line label"))
contentArea.addComponent(Label("This is another label on the second line"))
contentArea.addComponent(EmptySpace(TerminalSize(5, 1)))
contentArea.addComponent(Label("Here is a\nmulti-line\ntext segment that is using \\n"))
var label:Label? = Label("We can change foreground color...")
label!!.setForegroundColor(TextColor.ANSI.BLUE)
contentArea.addComponent(label)
label = Label("...and background color...")
label!!.setBackgroundColor(TextColor.ANSI.MAGENTA)
contentArea.addComponent(label)
label = Label("...and add custom SGR styles!")
label!!.addStyle(SGR.BOLD)
label!!.addStyle(SGR.UNDERLINE)
contentArea.addComponent(label)
contentArea.addComponent(EmptySpace(TerminalSize(5, 1)))
contentArea.addComponent(Label("Here is an animated label:"))
contentArea.addComponent(AnimatedLabel.createClassicSpinningLine())
contentArea.addComponent(EmptySpace())
contentArea.addComponent(Button("Close", Runnable({ window.close() })).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.CENTER)))

window.setComponent(contentArea)
textGUI.addWindow(window)
textGUI.updateScreen()
while (!textGUI.getWindows().isEmpty())
{
textGUI.processInput()
if (textGUI.isPendingUpdate())
{
textGUI.updateScreen()
}
else
{
Thread.sleep(1)
}
}
}

finally
{
screen!!.stopScreen()
}
}
}
