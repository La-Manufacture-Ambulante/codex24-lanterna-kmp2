package com.googlecode.lanterna.examples

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import java.io.IOException

/**
 * Creates a TerminalScreen and a TextGraphics from it and writes a rectangle
 * from the character '*'
 *
 * @author Peter Borkuti
 */
object DrawRectangle {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal =
            requireNotNull(DefaultTerminalFactory().createTerminal()) {
                "Failed to create terminal"
            }
        val screen = TerminalScreen(terminal)

        val tGraphics = screen.newTextGraphics()

        screen.startScreen()
        screen.clear()

        tGraphics!!.drawRectangle(
            TerminalPosition(3, 3), TerminalSize(10, 10), '*',
        )
        screen.refresh()

        screen.readInput()
        screen.stopScreen()
    }
}
