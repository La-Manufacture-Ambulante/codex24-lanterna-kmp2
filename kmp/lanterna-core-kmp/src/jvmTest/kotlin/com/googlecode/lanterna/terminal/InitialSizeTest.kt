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
import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TestTerminalFactory
import java.io.IOException

/**
 *
 * @author Martin
 */
object InitialSizeTest {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val rawTerminal = TestTerminalFactory(args).createTerminal()!!
        rawTerminal!!.enterPrivateMode()
        rawTerminal!!.clearScreen()

        rawTerminal!!.setCursorPosition(5, 5)
        printString(rawTerminal, "Waiting for initial size...")
        rawTerminal!!.flush()

        val initialSize = rawTerminal!!.terminalSize
        rawTerminal!!.clearScreen()
        rawTerminal!!.setCursorPosition(5, 5)
        printString(rawTerminal, "Initial size: ")
        rawTerminal!!.enableSGR(SGR.BOLD)
        printString(rawTerminal, initialSize!!.toString())
        rawTerminal!!.resetColorAndSGR()
        rawTerminal!!.flush()

        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
        }

        rawTerminal!!.exitPrivateMode()
    }

    @Throws(IOException::class)
    private fun printString(
        rawTerminal: Terminal,
        string: String,
    ) {
        for (i in 0 until string.length) {
            rawTerminal!!.putCharacter(string[i])
        }
    }
}
