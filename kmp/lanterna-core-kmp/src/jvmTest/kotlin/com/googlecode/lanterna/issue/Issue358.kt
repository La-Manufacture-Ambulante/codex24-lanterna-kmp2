package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import java.io.IOException

 object Issue358 {
@Throws(IOException::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()!!
val screen = TerminalScreen(terminal)
screen.startScreen()
val textGUI = MultiWindowTextGUI(screen, DefaultWindowManager(), EmptySpace(TextColor.ANSI.BLUE))

val title = "Issue358"

val nbColumns = 5
val window = BasicWindow(title)

val layoutManager = GridLayout(nbColumns)
layoutManager.setVerticalSpacing(0)
layoutManager.setHorizontalSpacing(1)
val contentPanel = Panel(layoutManager)
contentPanel.addComponent(
EmptySpace(TextColor.ANSI.CYAN).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, false, false, 3, 1)))
window.component = contentPanel
textGUI.addWindowAndWait(window)
}
}
