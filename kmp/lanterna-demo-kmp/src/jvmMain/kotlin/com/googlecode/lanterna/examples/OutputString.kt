package com.googlecode.lanterna.examples

import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

object OutputString {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal =
            requireNotNull(DefaultTerminalFactory().createTerminal()) {
                "Failed to create terminal"
            }
        val screen = TerminalScreen(terminal)

        val s = "Hello World!"
        val tGraphics = screen.newTextGraphics()

        screen.startScreen()
        screen.clear()

        tGraphics!!.putString(10, 10, s)
        screen.refresh()

        screen.readInput()
        screen.stopScreen()
    }
}
