package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import java.io.IOException

class OutputChar {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun main(args: Array<String?>?) {
            val terminal: Terminal = DefaultTerminalFactory().createTerminal()
            val screen: Screen = TerminalScreen(terminal)

            screen.startScreen()
            screen.clear()

            screen.setCharacter(10, 10, TextCharacter('*'))
            screen.refresh()

            screen.readInput()
            screen.stopScreen()
        }
    }
}
