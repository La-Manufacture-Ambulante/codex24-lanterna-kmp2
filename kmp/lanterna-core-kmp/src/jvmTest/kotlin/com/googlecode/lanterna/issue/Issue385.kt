package com.googlecode.lanterna.issue

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.util.Collections

object Issue385 {
    @Throws(Exception::class)
    fun main(args: Array<String?>?) {
        val terminalFactory = DefaultTerminalFactory()
        val screen = terminalFactory.createScreen()
        screen!!.startScreen()

        val textGUI = MultiWindowTextGUI(screen)

        val window = BasicWindow("My Root Window")
        window.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN))

        textGUI.addWindowAndWait(window)
    }
}
