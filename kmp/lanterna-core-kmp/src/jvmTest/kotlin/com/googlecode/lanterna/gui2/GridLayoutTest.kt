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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import java.io.IOException

class GridLayoutTest : TestBase() {
    fun init(textGUI: WindowBasedTextGUI) {
        val window = BasicWindow("Grid layout test")

        val leftGridPanel = Panel()
        leftGridPanel.setLayoutManager(GridLayout(4))
        leftGridPanel.addComponent(EmptySpace(TextColor.ANSI.BLACK, TerminalSize(4, 2)))
        leftGridPanel.addComponent(EmptySpace(TextColor.ANSI.BLUE, TerminalSize(4, 2)))
        val visibilityToggleableComponent = EmptySpace(TextColor.ANSI.CYAN, TerminalSize(4, 2))
        leftGridPanel.addComponent(visibilityToggleableComponent)
        leftGridPanel.addComponent(EmptySpace(TextColor.ANSI.GREEN, TerminalSize(4, 2)))

        leftGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.MAGENTA, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.CENTER, true, false, 4, 1)),
        )
        leftGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.RED, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 4, 1)),
        )
        leftGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.YELLOW, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER, true, false, 4, 1)),
        )
        leftGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.BLACK, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER, true, false, 4, 1)),
        )

        val rightGridPanel = Panel()
        rightGridPanel.setLayoutManager(GridLayout(5))
        rightGridPanel.addComponent(EmptySpace(TextColor.ANSI.BLACK, TerminalSize(4, 2)))
        rightGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.MAGENTA, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.BEGINNING, false, true, 1, 4)),
        )
        rightGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.RED, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, false, true, 1, 4)),
        )
        rightGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.YELLOW, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.END, false, true, 1, 4)),
        )
        rightGridPanel.addComponent(
            EmptySpace(TextColor.ANSI.BLACK, TerminalSize(4, 2))
                .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.FILL, false, true, 1, 4)),
        )
        rightGridPanel.addComponent(EmptySpace(TextColor.ANSI.BLUE, TerminalSize(4, 2)))
        rightGridPanel.addComponent(EmptySpace(TextColor.ANSI.CYAN, TerminalSize(4, 2)))
        rightGridPanel.addComponent(EmptySpace(TextColor.ANSI.GREEN, TerminalSize(4, 2)))

        val contentPanel = Panel()
        contentPanel.setLayoutManager(LinearLayout(Direction.VERTICAL))
        contentPanel.addComponent(Panels.horizontal(leftGridPanel, EmptySpace(TerminalSize.ONE), rightGridPanel))
        contentPanel.addComponent(EmptySpace(TerminalSize.ONE))
        contentPanel.addComponent(
            Panels.horizontal(
                Button("Toggle Visible Component", { visibilityToggleableComponent.setVisible(!visibilityToggleableComponent.isVisible) }),
                Button("Close", Runnable { window.close() }),
            ),
        )
        window.component = contentPanel
        textGUI.addWindow(window)
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            GridLayoutTest().run(args)
        }
    }
}
