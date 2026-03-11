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

import com.googlecode.lanterna.TestTerminalFactory
import com.googlecode.lanterna.TextColor
import java.io.IOException
import java.util.Random

/**
 * This class will try using the 24-bit color extension supported by a few terminal emulators
 *
 * @author Martin
 */
object Terminal24bitColorTest {
    @Throws(IOException::class)
    fun main(args: Array<String?>?) {
        val string = "Hello!"
        val random = Random()
        val terminal = TestTerminalFactory(args).createTerminal()!!
        terminal!!.enterPrivateMode()
        terminal!!.clearScreen()
        val size = terminal!!.terminalSize

        while (true) {
            if (terminal!!.pollInput() != null) {
                terminal!!.exitPrivateMode()
                return
            }

            terminal!!.setForegroundColor(TextColor.RGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
            terminal!!.setBackgroundColor(TextColor.RGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
            terminal!!.setCursorPosition(random.nextInt(size!!.columns - string.length), random.nextInt(size!!.rows))
            printString(terminal, string)

            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
            }
        }
    }

    @Throws(IOException::class)
    private fun printString(
        terminal: Terminal,
        string: String,
    ) {
        for (i in 0 until string.length) {
            terminal!!.putCharacter(string[i])
        }
        terminal!!.flush()
    }
}
