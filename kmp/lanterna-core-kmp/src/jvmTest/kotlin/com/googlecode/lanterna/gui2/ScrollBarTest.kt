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
import com.googlecode.lanterna.getRenderer
import com.googlecode.lanterna.getText
import com.googlecode.lanterna.internal.compat.Pattern
import java.io.IOException

class ScrollBarTest : TestBase() {
    @Override
    fun init(textGUI: WindowBasedTextGUI) {
        val basicWindow = BasicWindow("ScrollBar test")
        val contentPanel = Panel()
        contentPanel.setLayoutManager(GridLayout(2))

        val controlPanel = Panel()
        val checkVerticalTrackerGrow = CheckBox().setChecked(true)
        val checkHorizontalTrackerGrow = CheckBox().setChecked(true)
        val textBoxVerticalSize = TextBox("10").setValidationPattern(Pattern.compile("[0-9]+"))
        val textBoxHorizontalSize = TextBox("10").setValidationPattern(Pattern.compile("[0-9]+"))
        val textBoxVerticalPosition = TextBox("0").setValidationPattern(Pattern.compile("[0-9]+"))
        val textBoxHorizontalPosition = TextBox("0").setValidationPattern(Pattern.compile("[0-9]+"))
        val textBoxVerticalMax = TextBox("100").setValidationPattern(Pattern.compile("[0-9]+"))
        val textBoxHorizontalMax = TextBox("100").setValidationPattern(Pattern.compile("[0-9]+"))
        val verticalScroll = ScrollBar(Direction.VERTICAL)
        val horizontalScroll = ScrollBar(Direction.HORIZONTAL)
        val buttonRefresh =
            Button("Refresh", {
                (verticalScroll.getRenderer() as ScrollBar.DefaultScrollBarRenderer).setGrowScrollTracker(
                    checkVerticalTrackerGrow!!.isChecked(),
                )
                verticalScroll.setScrollMaximum(getInteger(textBoxVerticalMax!!.getText(), 100))
                verticalScroll.setScrollPosition(getInteger(textBoxVerticalPosition!!.getText(), 100))
                verticalScroll.setViewSize(getInteger(textBoxVerticalSize!!.getText(), 1))
                (horizontalScroll.getRenderer() as ScrollBar.DefaultScrollBarRenderer).setGrowScrollTracker(
                    checkHorizontalTrackerGrow!!.isChecked(),
                )
                horizontalScroll.setScrollMaximum(getInteger(textBoxHorizontalMax!!.getText(), 0))
                horizontalScroll.setScrollPosition(getInteger(textBoxHorizontalPosition!!.getText(), 0))
                horizontalScroll.setViewSize(getInteger(textBoxHorizontalSize!!.getText(), 1))
            })
        val closeButton = Button("Close", Runnable({ basicWindow.close() }))

        verticalScroll.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.FILL, false, true))
        horizontalScroll.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2))
        buttonRefresh.setLayoutData(
            GridLayout.createLayoutData(
                GridLayout.Alignment.CENTER,
                GridLayout.Alignment.BEGINNING,
                true,
                true,
                2,
                1,
            ),
        )

        contentPanel.addComponent(controlPanel.withBorder(Borders.singleLine("Control")))
        contentPanel.addComponent(verticalScroll)
        contentPanel.addComponent(horizontalScroll)

        controlPanel.setLayoutManager(GridLayout(2))
        controlPanel.addComponent(Label("Vertical tracker grows:")).addComponent(checkVerticalTrackerGrow)
        controlPanel.addComponent(Label("Vertical view size:")).addComponent(textBoxVerticalSize)
        controlPanel.addComponent(Label("Vertical scroll position:")).addComponent(textBoxVerticalPosition)
        controlPanel.addComponent(Label("Vertical scroll max:")).addComponent(textBoxVerticalMax)
        controlPanel.addComponent(EmptySpace(TerminalSize.ONE)).addComponent(EmptySpace(TerminalSize.ONE))
        controlPanel.addComponent(Label("Horizontal tracker grows:")).addComponent(checkHorizontalTrackerGrow)
        controlPanel.addComponent(Label("Horizontal view size:")).addComponent(textBoxHorizontalSize)
        controlPanel.addComponent(Label("Horizontal scroll position:")).addComponent(textBoxHorizontalPosition)
        controlPanel.addComponent(Label("Horizontal scroll max:")).addComponent(textBoxHorizontalMax)
        controlPanel.addComponent(EmptySpace(TerminalSize.ONE)).addComponent(EmptySpace(TerminalSize.ONE))
        controlPanel.addComponent(buttonRefresh)
        contentPanel.addComponent(closeButton)

        basicWindow.component = contentPanel
        textGUI.addWindow(basicWindow)
    }

    private fun getInteger(
        text: String?,
        defaultValue: Int,
    ): Int {
        try {
            return Integer.parseInt(text)
        } catch (e: NumberFormatException) {
            return defaultValue
        }
    }

    companion object {
        @Throws(IOException::class, InterruptedException::class)
        fun main(args: Array<String?>?) {
            ScrollBarTest().run(args)
        }
    }
}
