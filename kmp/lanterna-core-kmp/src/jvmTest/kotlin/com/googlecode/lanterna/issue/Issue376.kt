package com.googlecode.lanterna.issue

import com.googlecode.lanterna.*

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory

import java.io.IOException

 object Issue376 {
@Throws(IOException::class)
 fun main(vararg args:String?) {
val screen = DefaultTerminalFactory().createScreen()
screen!!.startScreen()
val gui = MultiWindowTextGUI(screen)
val window = LabelWithTabWindow()
gui.addWindow(window)
gui.waitForWindowToClose(window)
screen!!.stopScreen()
}

private class LabelWithTabWindow internal constructor():AbstractWindow() {
init{
val panel = Panel()
panel.addComponent(Label("A label without tab"))
panel.addComponent(Label("\tAnd one with tab"))
setComponent(panel)
}
}
}


