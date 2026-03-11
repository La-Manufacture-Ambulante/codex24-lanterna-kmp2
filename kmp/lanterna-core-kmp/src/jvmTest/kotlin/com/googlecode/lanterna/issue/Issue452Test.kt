/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
@file:Suppress("ktlint:standard:function-signature")

package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.*
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.TextBox.Style
import com.googlecode.lanterna.gui2.table.*
import com.googlecode.lanterna.input.*
import com.googlecode.lanterna.screen.*
import com.googlecode.lanterna.terminal.*
import com.googlecode.lanterna.terminal.virtual.DefaultVirtualTerminal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Automatic tests for mouse support. These test do not actually lauch
 * [TerminalScreen] as testing that is not the point. Point is to test how
 * different [Interactable]s handle mouse clicks.
 */
class Issue452Test {
    private var content: Panel? = null
    private var window: Window? = null

    @Before
    fun before() {
        window = BasicWindow("Issue452Test")
        content = Panel(GridLayout(GRID_WIDTH))
        val gridLayout = content!!.getLayoutManager() as GridLayout?
        gridLayout!!.setVerticalSpacing(1)
        window!!.position = TerminalPosition.TOP_LEFT_CORNER
        window!!.component = content
    }

/**
     * some tests need to have the component rendered in to get sizing
     */
    @Throws(Exception::class)
    internal fun displayForRenderering(component: Component?) {
        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
        // making use of the drawing routines in the renderer to know the size this thing is
        val terminal = DefaultVirtualTerminal(TerminalSize(100, 100))
        val screen = TerminalScreen(terminal)
        screen.startScreen()
        val textGUI = MultiWindowTextGUI(screen)
        val window = BasicWindow("needing to get the table drawn")
        window.component = component
        textGUI.addWindow(window)
        textGUI.updateScreen()
        // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    }

    @Test
    @Throws(Exception::class)
    fun testSingleLineTextBox() {
        val singleLine = TextBox("123456789")
        content!!.addComponent(singleLine, LAYOUT_NEW_ROW)
        displayForRenderering(singleLine)

        // Focus component
        clickOn(singleLine)
        assertTrue(singleLine.isFocused)
        // Click at 3rd position
        clickOnWithRelative(singleLine, 3, 0)
        singleLine.handleInput(KeyStroke(com.googlecode.lanterna.input.KeyType.BACKSPACE))
        // 3rd position (3) should be deleted
        assertEquals("12456789", singleLine.text)
    }

    @Test
    @Throws(Exception::class)
    fun testMultuLineTextBox() {
        val multiLine = TextBox("123456789\nabcdefgh", Style.MULTI_LINE)
        content!!.addComponent(multiLine, LAYOUT_NEW_ROW)
        displayForRenderering(multiLine)

        // Focus component
        clickOn(multiLine)
        assertTrue(multiLine.isFocused)
        // Click at 3rd position 1st row
        clickOnWithRelative(multiLine, 3, 0)
        multiLine.handleInput(KeyStroke(com.googlecode.lanterna.input.KeyType.BACKSPACE))
        // 3rd position (3) should be deleted
        assertEquals("12456789\nabcdefgh", multiLine.text)
        // Click at 5th position 2nd row
        clickOnWithRelative(multiLine, 5, 1)
        multiLine.handleInput(KeyStroke(com.googlecode.lanterna.input.KeyType.BACKSPACE))
        // 5th position (e) should be deleted
        assertEquals("12456789\nabcdfgh", multiLine.text)
    }

    @Test
    fun testCheckBox() {
        val checkBox = CheckBox("Checkbox")
        content!!.addComponent(checkBox, LAYOUT_NEW_ROW)
        assertFalse(checkBox.isFocused)
        assertFalse(checkBox.isChecked())
        // First click should focus the checkbox and item should be selected
        clickOn(checkBox)
        assertTrue(checkBox.isFocused)
        assertTrue(checkBox.isChecked())
        // Second click, focus should remain and item should be unselected
        clickOn(checkBox)
        assertTrue(checkBox.isFocused)
        assertFalse(checkBox.isChecked())
        // Third click should change its value back to TRUE
        clickOn(checkBox)
        assertTrue(checkBox.isFocused)
        assertTrue(checkBox.isChecked())
    }

    @Test
    fun testButton() {
        val button = Button("Button", createRunnable("Button"))
        content!!.addComponent(button, LAYOUT_NEW_ROW)
        assertFalse(button.isFocused)
        try {
            clickOn(button)
            fail()
        } catch (e: RunnableExecuted) {
            assertEquals("Button", e!!.name)
        }
    }

    @Test
    fun testActionListBox() {
        val listBox = ActionListBox()
        listBox.addItem(createRunnable("item_1"))
        listBox.addItem(createRunnable("item_2"))
        listBox.addItem(createRunnable("item_3"))
        listBox.addItem(createRunnable("item_4"))
        content!!.addComponent(listBox, LAYOUT_NEW_ROW)

        // First index is selected at the beginning so no need to focus it
        assertEquals(0, listBox.getSelectedIndex().toLong())

        try {
            listBox.handleInput(clickAt(0, 1))
            fail()
        } catch (e: RunnableExecuted) {
            assertEquals("item_2", e!!.name)
            assertEquals(1, listBox.getSelectedIndex().toLong())
        }

        try {
            listBox.handleInput(clickAt(0, 2))
            fail()
        } catch (e: RunnableExecuted) {
            assertEquals("item_3", e!!.name)
            assertEquals(2, listBox.getSelectedIndex().toLong())
        }

        try {
            listBox.handleInput(clickAt(0, 3))
            fail()
        } catch (e: RunnableExecuted) {
            assertEquals("item_4", e!!.name)
            assertEquals(3, listBox.getSelectedIndex().toLong())
        }
    }

    @Test
    fun testRadioBoxList() {
        val list = RadioBoxList<String?>()
        list.addItem("RadioGaga")
        list.addItem("RadioGogo")
        list.addItem("RadioBlaBla")
        content!!.addComponent(list, LAYOUT_NEW_ROW)
        // At start, first radio is selected but nothing should be checked
        assertEquals("RadioGaga", list.selectedItem)
        assertEquals(null, list.checkedItem)

        list.handleInput(clickAt(0, 1))
        // second radio should be selected and item should be checked
        assertEquals("RadioGogo", list.selectedItem)
        assertEquals("RadioGogo", list.checkedItem)

        list.handleInput(clickAt(0, 2))
        // third radio should be selected and item should be checked
        assertEquals("RadioBlaBla", list.selectedItem)
        assertEquals("RadioBlaBla", list.checkedItem)
    }

    @Test
    @Throws(Exception::class)
    fun testTable() {
        val table = Table<String?>("Column0000000", "Column111", "Column22222")
        table.setTheme(SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK))
        table.getTableModel()!!.addRow("0", "0", "0")
        table.getTableModel()!!.addRow("1", "1", "1")
        table.getTableModel()!!.addRow("2", "2", "2")
        table.setSelectAction(createRunnable("Table"))
        table.setCellSelection(true)
        content!!.addComponent(table, LAYOUT_NEW_ROW)
        displayForRenderering(table)

        assertTrue(table.isFocused)
        assertEquals(0, table.getSelectedColumn().toLong())
        assertEquals(0, table.getSelectedRow().toLong())

        // 0, 0 would get activated by first click so just to be able to run same method
        // for all indices sets position to 1, 1
        table.setSelectedColumn(1)
        table.setSelectedRow(1)

        // ease to set debugger breakpoints
        assertTablePositionSelectedAndExecuted(table, 0, 0)
        assertTablePositionSelectedAndExecuted(table, 1, 0)
        assertTablePositionSelectedAndExecuted(table, 2, 0)

        assertTablePositionSelectedAndExecuted(table, 0, 1)
        assertTablePositionSelectedAndExecuted(table, 1, 1)
        assertTablePositionSelectedAndExecuted(table, 2, 1)

        assertTablePositionSelectedAndExecuted(table, 0, 2)
        assertTablePositionSelectedAndExecuted(table, 1, 2)
        assertTablePositionSelectedAndExecuted(table, 2, 2)
    }

/**
     * Every click will have +1 on row because first row is column headers, columns
     * will have + length of previous column header lenghts because they span
     * multiple columns
     */
    private fun assertTablePositionSelectedAndExecuted(
        table: Table<String?>?,
        positionColumn: Int,
        positionRow: Int,
    ) {
        val headerPadding = 1 // should get this from renderer somehow...?

        var previousColumnsWidth = 0
        for (i in 0 until positionColumn) {
            previousColumnsWidth += table!!.getTableModel()!!.getColumnLabel(i)!!.length
            previousColumnsWidth += headerPadding
        }

        try {
            clickOnWithRelative(table!!, previousColumnsWidth, positionRow + 1)
            System.out.println(
                "--- Should not be here, should have thrown and caught exception from prior method call to " +
                    "clickOnWithRelative(table, positionColumn: " +
                    positionColumn + ", x: " + previousColumnsWidth + ", y: " + (positionRow + 1) + ")",
            )
            fail()
        } catch (e: RunnableExecuted) {
            assertEquals("Table", e!!.name)
            assertEquals("column:" + positionColumn, "column:" + table!!.getSelectedColumn())
            assertEquals("row:" + positionRow, "row:" + table!!.getSelectedRow())
        }
    }

/**
     * Clicks at position
     */
    private fun clickAt(
        column: Int,
        row: Int,
    ): MouseAction {
        return MouseAction(MouseActionType.CLICK_DOWN, 1, TerminalPosition(column, row))
    }

/**
     * Clicks at position of the [Interactable]
     */
    private fun clickOn(component: Interactable) {
        component.handleInput(clickAt(component.position!!.column, component.position!!.row))
    }

/**
     * Clicks at position of the [Interactable] with offset
     */
    private fun clickOnWithRelative(
        component: Interactable,
        column: Int,
        row: Int,
    ) {
        val mouseAction = clickAt(component.globalPosition!!.column + column, component.globalPosition!!.row + row)
        component.handleInput(mouseAction)
    }

    private fun createRunnable(name: String?): Runnable {
        return Runnable {
            // propagate that this runnable was executed
            throw RunnableExecuted(name)
        }
    }

    private inner class RunnableExecuted(val name: String?) : RuntimeException()

    companion object {
        private val GRID_WIDTH = 100
        private val LAYOUT_NEW_ROW = GridLayout.createHorizontallyFilledLayoutData(GRID_WIDTH)
    }
}
