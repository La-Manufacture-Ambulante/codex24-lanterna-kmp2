package com.googlecode.lanterna.issue

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

 object Issue460 {
@Throws(Exception::class)
 fun main(args:Array<String?>?) {
val terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)
screen.startScreen()

val window1 = BasicWindow()
val contentPanel = Panel(GridLayout(1))
contentPanel.addComponent(Label("VERTICAL"), GridLayout.createLayoutData(
GridLayout.Alignment.CENTER, 
GridLayout.Alignment.CENTER, 
true, 
true, 
1, 
4
))
contentPanel.addComponent(Button("Close", object:Runnable() {
@Override
@JvmStatic  fun run() {
window1.close()
}
}), GridLayout.createHorizontallyFilledLayoutData(2))
window1.setComponent(contentPanel)

 // Create gui and start gui
        val gui = MultiWindowTextGUI(screen)
gui.addWindowAndWait(window1)
screen.stopScreen()
}
}
