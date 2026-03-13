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
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.setCursorPosition
import java.io.IOException

/**
 *
 * @author martin
 */
object TerminalInputTest {
    @Throws(InterruptedException::class, IOException::class)
    fun main(args: Array<String?>?) {
        // For IDE users: either set runtime arguments or uncomment this line:
        // args = new String[] { "--mouse-move", "--telnet-port=1024", "--with-timeout=12" };

        val rawTerminal = TestTerminalFactory(args).createTerminal()!!
        rawTerminal!!.enterPrivateMode()

        var currentRow = 0
        rawTerminal!!.setCursorPosition(0, 0)
        while (true) {
            val key = rawTerminal!!.pollInput()
            if (key == null) {
                Thread.sleep(1)
                continue
            }

            if (key!!.keyType == KeyType.ESCAPE || key!!.keyType == KeyType.EOF) {
                break
            }

            if (currentRow == 0) {
                rawTerminal!!.clearScreen()
            }

            rawTerminal!!.setCursorPosition(0, currentRow++)
            putString(rawTerminal, key!!.toString())

            if (currentRow >= rawTerminal!!.terminalSize!!.rows) {
                currentRow = 0
            }
        }

        rawTerminal!!.exitPrivateMode()
    }

    @Throws(IOException::class)
    private fun putString(
        rawTerminal: Terminal,
        string: String,
    ) {
        for (i in 0 until string.length) {
            rawTerminal!!.putCharacter(string[i])
        }
        rawTerminal!!.flush()
    }
}
