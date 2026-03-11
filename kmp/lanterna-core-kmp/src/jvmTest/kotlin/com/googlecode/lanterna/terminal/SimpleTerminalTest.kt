/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.*
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import java.awt.*
import java.io.IOException

object SimpleTerminalTest {
    private val COLORS_TO_CYCLE =
        arrayOf<TextColor?>(TextColor.ANSI.BLUE, TextColor.ANSI.CYAN, TextColor.ANSI.GREEN, TextColor.ANSI.MAGENTA, TextColor.ANSI.RED, TextColor.ANSI.WHITE, TextColor.ANSI.YELLOW)

    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val terminal = TestTerminalFactory(args).createTerminal()
        val textGraphics = terminal!!.newTextGraphics()
        var inPrivateMode = false
        var colorIndex = 0
        terminal!!.clearScreen()
        printHelp(textGraphics!!)
        terminal!!.putCharacter('\n')
        terminal!!.setBackgroundColor(COLORS_TO_CYCLE[0])
        var cursorPosition: TerminalPosition? = resetCursorPositionAfterHelp(terminal!!)
        terminal!!.flush()

        mainLoop@ while (true) {
            val keyStroke = terminal!!.readInput()
            when (keyStroke!!.keyType) {
                KeyType.ESCAPE, KeyType.EOF -> break@mainLoop

                KeyType.ARROW_DOWN ->
                    if ((terminal!!.terminalSize?.rows ?: 0) > cursorPosition!!.row + 1) {
                        cursorPosition = cursorPosition!!.withRelativeRow(1)
                        terminal!!.setCursorPosition(cursorPosition!!.column, cursorPosition!!.row)
                    }
                KeyType.ARROW_UP ->
                    if (cursorPosition!!.row > 0) {
                        cursorPosition = cursorPosition!!.withRelativeRow(-1)
                        terminal!!.setCursorPosition(cursorPosition!!.column, cursorPosition!!.row)
                    }
                KeyType.ARROW_RIGHT ->
                    if (cursorPosition!!.column + 1 < (terminal!!.terminalSize?.columns ?: 0)) {
                        cursorPosition = cursorPosition!!.withRelativeColumn(1)
                        terminal!!.setCursorPosition(cursorPosition!!.column, cursorPosition!!.row)
                    }
                KeyType.ARROW_LEFT ->
                    if (cursorPosition!!.column > 0) {
                        cursorPosition = cursorPosition!!.withRelativeColumn(-1)
                        terminal!!.setCursorPosition(cursorPosition!!.column, cursorPosition!!.row)
                    }

                KeyType.CHARACTER ->
                    when (keyStroke.character) {
                        '?' -> {
                            terminal!!.putCharacter('\n')
                            printHelp(textGraphics!!)
                            cursorPosition = resetCursorPositionAfterHelp(terminal!!)
                        }
                        'm' ->
                            if (inPrivateMode) {
                                terminal!!.exitPrivateMode()
                                inPrivateMode = false
                            } else {
                                terminal!!.enterPrivateMode()
                                inPrivateMode = true
                            }
                        'n' -> {
                            terminal!!.putCharacter('\n')
                            cursorPosition = cursorPosition!!.withRelativeRow(1)!!.withColumn(0)
                        }
                        'b' -> terminal!!.bell()
                        'c' -> {
                            colorIndex++
                            if (colorIndex >= COLORS_TO_CYCLE.size) {
                                colorIndex = 0
                            }
                            terminal!!.setBackgroundColor(COLORS_TO_CYCLE[colorIndex])
                        }
                        'p' -> {
                            val position = terminal!!.cursorPosition
                            textGraphics!!.putString(1, (terminal!!.terminalSize?.rows ?: 1) - 1, position.toString() + "                                     ")

                            // Restore the background color which was reset in the call above
                            terminal!!.setBackgroundColor(COLORS_TO_CYCLE[colorIndex])

                            terminal!!.setCursorPosition(position!!.column, position.row)
                        }

                        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            val ch = keyStroke.character ?: break@mainLoop
                            for (i in 0 until Integer.parseInt(ch.toString())) {
                                terminal!!.putCharacter(' ')
                            }
                            cursorPosition = terminal!!.cursorPosition
                        }
                        else -> {}
                    }
                else -> {}
            }
            terminal!!.flush()
        }
        if (inPrivateMode) {
            terminal!!.exitPrivateMode()
        }
        terminal!!.setBackgroundColor(TextColor.ANSI.DEFAULT)
        terminal!!.setForegroundColor(TextColor.ANSI.DEFAULT)
        terminal!!.putCharacter('\n')
        terminal!!.flush()

        if (terminal is Window) {
            (terminal as Window).dispose()
        }
    }

    @Throws(IOException::class)
    private fun resetCursorPositionAfterHelp(terminal: Terminal): TerminalPosition {
        val cursorPosition = TerminalPosition(0, 10)
        terminal.setCursorPosition(cursorPosition.column, cursorPosition.row)
        return cursorPosition
    }

    private fun printHelp(textGraphics: TextGraphics) {
        textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT)
        textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT)
        textGraphics.putString(1, 0, "Commands available:")
        textGraphics.putString(1, 1, "?            - Print this message")
        textGraphics.putString(1, 2, "m            - Toggle private mode on/off")
        textGraphics.putString(1, 3, "n            - Newline")
        textGraphics.putString(1, 4, "b            - Bell")
        textGraphics.putString(1, 5, "c            - Cycle color")
        textGraphics.putString(1, 6, "p            - Print cursor position")
        textGraphics.putString(1, 7, "<arrow keys> - Move cursor")
        textGraphics.putString(1, 8, "1-9          - Print X number of blocks at cursor")
    }
}
