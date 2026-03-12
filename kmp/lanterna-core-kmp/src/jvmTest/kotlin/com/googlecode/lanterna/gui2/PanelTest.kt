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

import java.io.IOException

class PanelTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Grid layout test")

        val mainPanel = Panel()
        mainPanel.setLayoutManager(LinearLayout(Direction.HORIZONTAL))

        val leftPanel = Panel()
        mainPanel.addComponent(leftPanel.withBorder(Borders.singleLine("Left")))

        var panel: Panel? = Panel()
        panel!!.addComponent(Button("Panel 1 Button"))
        leftPanel.addComponent(panel!!.withBorder(Borders.singleLine()))
        panel = Panel()
        panel!!.addComponent(Button("Panel 2 Button"))
        leftPanel.addComponent(panel!!.withBorder(Borders.singleLine("Title")))
        panel = Panel()
        panel!!.addComponent(Button("Panel 3 Button"))
        leftPanel.addComponent(panel!!.withBorder(Borders.doubleLine()))
        panel = Panel()
        panel!!.addComponent(Button("Panel 4 Button"))
        leftPanel.addComponent(panel!!.withBorder(Borders.doubleLine("Title")))

        val rightPanel = Panel()
        mainPanel.addComponent(rightPanel.withBorder(Borders.singleLine("Right")))

        panel = Panel()
        panel!!.addComponent(Button("Panel 1 Button"))
        panel!!.addComponent(Panel().withBorder(Borders.singleLine("A")))
        panel!!.addComponent(Panel().withBorder(Borders.singleLine("Some Text")))
        rightPanel.addComponent(panel!!.withBorder(Borders.singleLine("B")))
        panel = Panel()
        panel!!.addComponent(Button("Panel 2 Button"))
        rightPanel.addComponent(panel!!.withBorder(Borders.singleLine("Title")))
        panel = Panel()
        panel!!.addComponent(Button("Panel 3 Button"))
        rightPanel.addComponent(panel!!.withBorder(Borders.doubleLine()))
        panel = Panel()
        panel!!.addComponent(Button("Panel 4 Button"))
        rightPanel.addComponent(panel!!.withBorder(Borders.doubleLine("Title")))

        window.component =
            Panels.vertical(
                mainPanel.withBorder(Borders.singleLine("Main")),
                Button("OK", Runnable { window.close() }),
            )
        textGUI.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            PanelTest().run(args)
        }
    }
}
