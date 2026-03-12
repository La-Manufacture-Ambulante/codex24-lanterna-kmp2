package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

/**
 * Creates a terminal and prints a '*' to the (10,10) position.
 * Waits for a keypress then exit.
 *
 * @author Peter Borkuti
 */
object OutputChar {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal =
            requireNotNull(DefaultTerminalFactory().createTerminal()) {
                "Failed to create terminal"
            }
        val screen = TerminalScreen(terminal)

        screen.startScreen()
        screen.clear()

        screen.setCharacter(10, 10, TextCharacter('*'))
        screen.refresh()

        screen.readInput()
        screen.stopScreen()
    }
}
