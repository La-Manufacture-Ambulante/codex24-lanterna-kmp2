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
import java.io.IOException

/**
 * Simple test for the different kinds of list boxes
 * @author Martin
 */
class ListBoxTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("ListBox test")

        val horizontalPanel = Panel()
        horizontalPanel.setLayoutManager(LinearLayout(Direction.HORIZONTAL))

        val size = TerminalSize(14, 10)
        val checkBoxList = CheckBoxList<String?>(size)
        val radioBoxList = RadioBoxList<String?>(size)
        val actionListBox = ActionListBox(size)
        for (i in 0..29) {
            val itemText = "Item " + (i + 1)
            checkBoxList.addItem(itemText)
            radioBoxList.addItem(itemText)
            actionListBox.addItem(itemText, { System.out.println("Selected " + itemText) })
        }
        horizontalPanel.addComponent(checkBoxList.withBorder(Borders.singleLine("CheckBoxList")))
        horizontalPanel.addComponent(radioBoxList.withBorder(Borders.singleLine("RadioBoxList")))
        horizontalPanel.addComponent(actionListBox.withBorder(Borders.singleLine("ActionListBox")))

        window.component =
            Panels.vertical(
                horizontalPanel,
                Button("OK", Runnable { window.close() }),
            )
        textGUI.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            ListBoxTest().run(args)
        }
    }
}
