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
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.DefaultWindowManager
import com.googlecode.lanterna.gui2.EmptySpace
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object Issue221 {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        // Setup terminal and screen layers
        val terminal = DefaultTerminalFactory().createTerminal()!!
        val screen = TerminalScreen(terminal)
        screen.startScreen()

        // Create panel to hold components
        val panel = Panel()
        panel.setLayoutManager(GridLayout(2))

        panel.addComponent(Label("The List"))
        val box = RadioBoxList<String>()
        box.addItem("Item 1")
        box.addItem("Item 2")
        box.addItem("Item 3")
        box.addListener(
            object : RadioBoxList.Listener {
                override fun onSelectionChanged(
                    selectedIndex: Int,
                    previousSelection: Int,
                ) {
                    System.out.println("Selected Index: " + selectedIndex + ", previous: " + previousSelection)
                }
            },
        )

        panel.addComponent(box)

        // Create window to hold the panel
        val window = BasicWindow()
        window.component = panel

        // Create gui and start gui
        val gui = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))
        gui.addWindowAndWait(window)
    }
}
