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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.ansi.TelnetTerminal
import com.googlecode.lanterna.terminal.ansi.TelnetTerminalServer
import java.io.IOException
import java.net.SocketException

object GUIOverTelnet {
    @Throws(IOException::class)
    
    fun main(args: Array<String?>?) {
        val telnetTerminalServer = TelnetTerminalServer(1024)
        println("Listening on port 1024, please connect with a telnet client")

        while (true) {
            val telnetTerminal = telnetTerminalServer.acceptConnection() ?: continue
            println("Accepted connection from ${telnetTerminal.remoteSocketAddress}")
            Thread {
                try {
                    runGUI(telnetTerminal)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        telnetTerminal.close()
                    } catch (_: IOException) {
                    }
                }
            }.start()
        }
    }

    @Throws(IOException::class)
    private fun runGUI(telnetTerminal: TelnetTerminal) {
        val screen = TerminalScreen(telnetTerminal)
        screen.startScreen()
        val textGUI = MultiWindowTextGUI(screen)
        textGUI.setBlockingIO(false)
        textGUI.isEOFWhenNoWindows = true

        try {
            val window = BasicWindow("Text GUI over Telnet")
            val contentArea = Panel()
            contentArea.setLayoutManager(LinearLayout(Direction.VERTICAL))

            val textBox = TextBox(TerminalSize(30, 4))
            textBox.withBorder(Borders.singleLine("Text editor"))

            val openMessageButton = Button("Button", Runnable {
                val messageBox = BasicWindow("Response")
                messageBox.component = Panels.vertical(
                    Label("Hello!"),
                    Button("Close", Runnable { messageBox.close() }),
                )
                textGUI.addWindow(messageBox)
            })
            openMessageButton.withBorder(Borders.singleLine("This is a button"))

            contentArea.addComponent(openMessageButton)
            contentArea.addComponent(textBox)
            contentArea.addComponent(Button("Close", Runnable { window.close() }))
            window.component = contentArea

            textGUI.addWindowAndWait(window)
        } finally {
            try {
                screen.stopScreen()
            } catch (_: SocketException) {
                // telnet client may disconnect abruptly; ignore
            }
        }
    }
}
