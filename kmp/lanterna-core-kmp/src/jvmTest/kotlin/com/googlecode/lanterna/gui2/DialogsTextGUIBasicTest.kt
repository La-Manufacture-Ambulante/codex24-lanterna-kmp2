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
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.gui2.dialogs.*
import com.googlecode.lanterna.screen.Screen

import java.io.File
import java.io.IOException
import java.util.regex.Pattern

/**
 * 
 * @author Martin
 */
 object DialogsTextGUIBasicTest {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val screen = TestTerminalFactory(args).createScreen()
screen!!.startScreen()
val textGUI = MultiWindowTextGUI(screen)
try
{
val window = BasicWindow("Dialog test")

val mainPanel = Panel()
val dialogsListBox = ActionListBox()
dialogsListBox.addItem("Simple TextInputDialog", { val result = TextInputDialog.showDialog(textGUI, "TextInputDialog sample", "This is the description", "initialContent")
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("Password input", { val result = TextInputDialog.showPasswordDialog(textGUI, "Test password input", "This is a password input dialog", "")
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("Multi-line input", { val result = TextInputDialogBuilder()
.setTitle("Multi-line editor")
.setTextBoxSize(TerminalSize(35, 5))
.build()
.showDialog(textGUI)
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("Numeric input", { val result = TextInputDialogBuilder()
.setTitle("Numeric input")
.setDescription("Enter a number")
.setValidationPattern(Pattern.compile("[0-9]+"), "Please enter a valid number")
.build()
.showDialog(textGUI)
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("File dialog (open)", { val result = FileDialogBuilder()
.setTitle("Open File")
.setDescription("Choose a file:")
.setActionLabel(LocalizedString.Open.toString())
.build()
.showDialog(textGUI)
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("File dialog (save)", { val result = FileDialogBuilder()
.setTitle("Save File")
.setDescription("Choose a file:")
.setActionLabel(LocalizedString.Save.toString())
.build()
.showDialog(textGUI)
System.out.println("Result was: " + result!!) })
dialogsListBox.addItem("Action list dialog", { ActionListDialogBuilder()
.setTitle("Action List Dialog")
.setDescription("Choose an item")
.addAction("First Item", { MessageDialog.showMessageDialog(textGUI, "Action List Dialog", "You chose First Item", MessageDialogButton.OK) })
.addAction("Second Item", { MessageDialog.showMessageDialog(textGUI, "Action List Dialog", "You chose Second Item", MessageDialogButton.OK) })
.addAction("Third Item", { MessageDialog.showMessageDialog(textGUI, "Action List Dialog", "You chose Third Item", MessageDialogButton.OK) })
.build()
.showDialog(textGUI) })

mainPanel.addComponent(dialogsListBox)
mainPanel.addComponent(EmptySpace(TerminalSize.ONE))
mainPanel.addComponent(Button("Exit", Runnable { window.close() }))
window.component = mainPanel

textGUI.addWindowAndWait(window)
}

finally
{
screen!!.stopScreen()
}
}
}
