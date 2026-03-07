/*
 * Author Andrey Zelyaev(zella), slightly modified by Andreas(avl42)
 */
package com.googlecode.lanterna.issue

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.screen.*
import com.googlecode.lanterna.terminal.*

import java.util.Collections

 object Issue361 {

@Throws(Exception::class)
 fun main(args:Array<String?>?) {

val terminal = DefaultTerminalFactory().createTerminal()
val screen = TerminalScreen(terminal)
screen.startScreen()

val window1 = BasicWindow()
window1.setHints(Collections.singletonList(Window.Hint.CENTERED))

val window2 = BasicWindow()
window2.setHints(Collections.emptyList<Window.Hint?>())

 // Create gui and start gui
        val gui = MultiWindowTextGUI(screen)
gui.addWindow(window2)
gui.addWindowAndWait(window1)
}
}
