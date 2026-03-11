package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

import java.io.IOException

 object Issue387 {

 fun main(args:Array<String?>?) {
try
{
val screen = DefaultTerminalFactory().createScreen()
screen!!.startScreen()

val window = BasicWindow()

val table = Table<String>("Column")
table.setVisibleRows(3)

table.getTableModel().addRow("row 1")
table.getTableModel().addRow("row 2")
table.getTableModel().addRow("row 3")
table.getTableModel().addRow("row 4")
table.getTableModel().addRow("row 5")
table.getTableModel().addRow("row 6")
table.getTableModel().addRow("row 7")

val panel = Panel()
panel.addComponent(TextBox())
panel.addComponent(EmptySpace(TerminalSize(15, 1)))
panel.addComponent(table)
panel.addComponent(EmptySpace(TerminalSize(15, 1)))
panel.addComponent(TextBox())

window.component = panel

val gui = MultiWindowTextGUI(screen)
gui.addWindowAndWait(window)
}
catch (e:IOException) {
e!!.printStackTrace()
}

}
}
