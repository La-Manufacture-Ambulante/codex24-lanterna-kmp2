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

package com.googlecode.lanterna
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame

/**
 * This class provides a unified way for test programs to obtain terminal objects.
 */
class TestTerminalFactory {
    private val delegate = DefaultTerminalFactory()

    constructor()

    constructor(args: Array<String?>?) {
        parseArgs(args)
    }

    fun parseArgs(args: Array<String?>?) {
        for (rawArg in args.orEmpty()) {
            val arg = rawArg ?: continue
            val tok = arg.split("=", limit = 2)
            val argName = tok[0]
            val par = if (tok.size > 1) tok[1] else ""
            when (argName) {
                "--text-terminal", "--no-swing" -> {
                    delegate.setPreferTerminalEmulator(false)
                    delegate.setForceTextTerminal(true)
                }

                "--awt" -> {
                    delegate.setForceTextTerminal(false)
                    delegate.setPreferTerminalEmulator(true)
                    delegate.setForceAWTOverSwing(true)
                }

                "--swing" -> {
                    delegate.setForceTextTerminal(false)
                    delegate.setPreferTerminalEmulator(true)
                    delegate.setForceAWTOverSwing(false)
                }

                "--mouse-click" -> delegate.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE)
                "--mouse-drag" -> delegate.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG)
                "--mouse-move" -> delegate.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE)

                "--telnet-port" -> {
                    val port = par.toIntOrNull() ?: 1024
                    delegate.setTelnetPort(port)
                }

                "--with-timeout" -> {
                    val inputTimeout = par.toIntOrNull() ?: 40
                    delegate.setInputTimeout(inputTimeout)
                }
            }
        }
    }

    fun createTerminal(): Terminal? = delegate.createTerminal()

    fun createScreen(): TerminalScreen = delegate.createScreen()

    fun createSwingTerminal(): SwingTerminalFrame = delegate.createSwingTerminal()
}
