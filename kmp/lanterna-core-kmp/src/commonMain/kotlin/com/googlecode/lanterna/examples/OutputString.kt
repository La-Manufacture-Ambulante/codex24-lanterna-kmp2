package com.googlecode.lanterna.examples

import java.io.IOException
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

class OutputString {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun main(args: Array<String?>?) {
            val terminal: Terminal = DefaultTerminalFactory().createTerminal()
            val screen: Screen = TerminalScreen(terminal)

            val s: String = "Hello World!"
            val tGraphics: TextGraphics = screen.newTextGraphics()

            screen.startScreen()
            screen.clear()

            tGraphics.putString(10, 10, s)
            screen.refresh()

            screen.readInput()
            screen.stopScreen()
        }
    }
}
