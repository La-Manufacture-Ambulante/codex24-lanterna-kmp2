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
package com.googlecode.lanterna.issue

import java.io.IOException

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Button.Listener
import com.googlecode.lanterna.gui2.CheckBox
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.LayoutData
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.TextBox.Style
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.menu.Menu
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.gui2.menu.MenuItem
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.MouseCaptureMode

/**
 * 
 * 
 * Serves as showcase for all [Interactable] components for manual testing
 * during development of mouse support. Uses Telnet port 23000 as you need
 * something different than swing terminal provided by IDE. After launching main
 * method you can connect to it via terminal "telnet localhost 23000" (or
 * something of that nature)
 * 
 * 
 * Automatic tests can be found in [Issue452Test].
 */
 object Issue452 {

private val GRID_WIDTH = 100
private val LAYOUT_NEW_ROW = GridLayout.createHorizontallyFilledLayoutData(GRID_WIDTH)
private var buttonTriggeredCounter = 0
private var actionListTextBox:TextBox? = null
private var menuTextBox:TextBox? = null
private var tableTextBox:TextBox? = null
private var tableTriggeredCounter = 0

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
DefaultTerminalFactory().setTelnetPort(23000)
.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE).setInitialTerminalSize(TerminalSize(100, 100))
.createScreen().use({ screen->
screen!!.startScreen()
val gui = MultiWindowTextGUI(screen)
val window = BasicWindow("Issue452")
val content = Panel(GridLayout(GRID_WIDTH))
val gridLayout = content.getLayoutManager() as GridLayout
gridLayout!!.setVerticalSpacing(1)
addInteractableComponentsToContent(content)
addMenuBar(window)
window.setComponent(content)
gui.addWindowAndWait(window) })
}

private fun addInteractableComponentsToContent(content:Panel) {
 // for menu bar so you know which menu you have triggered
        menuTextBox = TextBox("Try menu above")
content.addComponent(menuTextBox, LAYOUT_NEW_ROW)

 // single line textbox
        content.addComponent(TextBox("Single line TextBox"), LAYOUT_NEW_ROW)

 // multi line textbox
        content.addComponent(TextBox(
"First line of multi line TextBox" + System.lineSeparator() + "Second line of multi line TextBox", 
Style.MULTI_LINE), LAYOUT_NEW_ROW)

 // checkbox
        content.addComponent(CheckBox("CheckBox"), LAYOUT_NEW_ROW)

 // button
        val textBoxButton = TextBox("Click the button!")
val button = Button("Button")
button.addListener(object:Listener() {
@Override
 fun onTriggered(button:Button?) {
textBoxButton.setText("Button triggered " + Issue452.buttonTriggeredCounter++ + " times")
}
})
content.addComponent(button, GridLayout.createHorizontallyFilledLayoutData(1))
content.addComponent(textBoxButton, GridLayout.createHorizontallyFilledLayoutData(GRID_WIDTH - 1))

 // action list box
        actionListTextBox = TextBox("Click on something in the action list!")
val actionMenu = ActionListBox()
actionMenu.addItem("First menu", object:Runnable() {
@Override
@JvmStatic  fun run() {
actionListTextBox!!.setText("First menu clicked")
}
})
actionMenu.addItem("Second menu", object:Runnable() {
@Override
@JvmStatic  fun run() {
actionListTextBox!!.setText("Second menu clicked")
}
})
actionMenu.addItem("Third menu", object:Runnable() {
@Override
@JvmStatic  fun run() {
actionListTextBox!!.setText("Third menu clicked")
}
})
content.addComponent(actionListTextBox, LAYOUT_NEW_ROW)
content.addComponent(actionMenu, LAYOUT_NEW_ROW)

 // radiobox list
        val list = RadioBoxList()
list.addItem("RadioGaga")
list.addItem("RadioGogo")
list.addItem("RadioBlaBla")
content.addComponent(list, LAYOUT_NEW_ROW)

 // Table
        tableTextBox = TextBox("Try table bellow")
val table = Table("Column0000000", "Column111", "Column22222")
table.getTableModel().addRow("0", "0", "0")
table.getTableModel().addRow("1", "1", "1")
table.getTableModel().addRow("2", "2", "2")
table.setSelectAction({ tableTriggeredCounter++
tableTextBox!!.setText("Table's action runned " + tableTriggeredCounter + " times") })
table.setCellSelection(true)
content.addComponent(tableTextBox, LAYOUT_NEW_ROW)
content.addComponent(table, LAYOUT_NEW_ROW)
}

private fun addMenuBar(window:Window) {
val menuBar = MenuBar()
val menu = Menu("Settings")
menu.add(MenuItem("Menu1", object:Runnable() {
@Override
@JvmStatic  fun run() {
menuTextBox!!.setText("Menu1 clicked")
menuTextBox!!.invalidate()
}
}))
menu.add(MenuItem("Menu2", object:Runnable() {

@Override
@JvmStatic  fun run() {
menuTextBox!!.setText("Menu2 clicked")
menuTextBox!!.invalidate()
}
}))
menu.add(MenuItem("Menu3", object:Runnable() {

@Override
@JvmStatic  fun run() {
menuTextBox!!.setText("Menu3 clicked")
menuTextBox!!.invalidate()
}
}))
menuBar.add(menu)
window.setMenuBar(menuBar)
}

}