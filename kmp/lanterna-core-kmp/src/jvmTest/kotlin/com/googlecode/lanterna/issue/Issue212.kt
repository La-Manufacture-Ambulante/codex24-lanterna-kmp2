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
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object Issue212 {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val table =
            Table<String?>(
                "Column 1",
                "Column 2",
                "Column 3",
            )
        table.getTableModel()!!.addRow("1", "2", "3")
        table.getTableModel()!!.addRow("1", "2", "3")
        table.getTableModel()!!.addRow("1", "2", "3")
        table.getTableModel()!!.addRow("1", "2", "3")
        table.getTableModel()!!.addRow("1", "2", "3")
        table.setSelectAction({
            val data =
                table.getTableModel()!!.getRow(
                    table.getSelectedRow(),
                )
            for (aData in data!!) {
                System.out.println(aData)
            }
        })

        val win = BasicWindow()
        win.component = table

        val factory = DefaultTerminalFactory()
        val terminal = factory.createTerminal()!!

        val screen = TerminalScreen(terminal)
        screen.startScreen()

        // Create gui and start gui
        val gui =
            MultiWindowTextGUI(
                screen,
                DefaultWindowManager(),
                EmptySpace(TextColor.ANSI.BLUE),
            )
        gui.addWindowAndWait(win)

        screen.stopScreen()
    }
}
