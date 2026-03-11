package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.LinearLayout
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.menu.Menu
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.gui2.menu.MenuItem
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object Issue446 {

@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminalFactory = DefaultTerminalFactory()
val terminal = terminalFactory.createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()
val textGUI = MultiWindowTextGUI(screen)
textGUI.addWindowAndWait(buildWindow())
}

 fun buildWindow():BasicWindow {
val menuBar = MenuBar()
menuBar.add(Menu("Menu 1").add(MenuItem("MenuItem 1.1")))

val basicWindow = BasicWindow()

val textBox = TextBox("A")

val mainPanel = Panel(LinearLayout(Direction.VERTICAL))
mainPanel.addComponent(textBox)
mainPanel.addComponent(Button("Quit", Runnable { basicWindow.close() }))

basicWindow.setComponent(mainPanel)
basicWindow.setMenuBar(menuBar)
return basicWindow
}
}
