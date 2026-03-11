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
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object Issue216 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()

 // Create panel to hold components
        val panel = Panel()
panel.setLayoutManager(GridLayout(2))

panel.addComponent(Label("Forename"))
panel.addComponent(TextBox())

panel.addComponent(Label("Surname"))
panel.addComponent(TextBox())

panel.addComponent(Label("Table"))
val table = Table<String?>("Test")
val tableModel = table.getTableModel()
tableModel!!.addRow("hi")
panel.addComponent(table)

panel.addComponent(EmptySpace(TerminalSize(0, 0))) // Empty space underneath labels
panel.addComponent(Button("Submit", { tableModel!!.addRow("haiiii") 
 //table.invalidate();
        }))

 // Create window to hold the panel
        val window = BasicWindow()
window.setComponent(panel)

 // Create gui and start gui
        val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
gui.addWindowAndWait(window)
}
}
