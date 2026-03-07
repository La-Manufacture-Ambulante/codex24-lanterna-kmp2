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

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object Issue150 {
@Throws(IOException::class)
 fun main(vararg args:String?) {
val term = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(term)
val windowManager = DefaultWindowManager()
val background = EmptySpace(TextColor.ANSI.DEFAULT)
val gui = MultiWindowTextGUI(screen, windowManager, background)
screen.startScreen()
gui.addWindowAndWait(object:BasicWindow("Issue150") {
init{
setComponent(createUi())
}
})
screen.stopScreen()
}

private fun createUi():Component {
val actions = ActionListBox()
actions.addItem("Enter terminal in a strange state", ???({ stub() }))
return actions
}

private fun <T> stub():T? {
throw UnsupportedOperationException("What a terrible failure!")
}
}
