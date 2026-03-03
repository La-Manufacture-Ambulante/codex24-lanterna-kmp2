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

import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary

/**
 * Interface to Wincon, module in Win32 that can operate on the terminal
 */
internal interface Wincon : StdCallLibrary {
    companion object {
        @JvmField
        val STD_INPUT_HANDLE: Int = -10

        @JvmField
        val STD_OUTPUT_HANDLE: Int = -11

        // SetConsoleMode input values
        @JvmField
        val ENABLE_PROCESSED_INPUT: Int = 1

        @JvmField
        val ENABLE_LINE_INPUT: Int = 2

        @JvmField
        val ENABLE_ECHO_INPUT: Int = 4

        // SetConsoleMode screen buffer values
        @JvmField
        val ENABLE_VIRTUAL_TERMINAL_PROCESSING: Int = 4

        @JvmField
        val DISABLE_NEWLINE_AUTO_RETURN: Int = 8
    }

    fun GetStdHandle(var1: Int): WinDef.HANDLE?
    fun GetConsoleMode(var1: WinDef.HANDLE?, var2: IntByReference?): Boolean
    fun SetConsoleMode(var1: WinDef.HANDLE?, var2: Int): Boolean
    fun GetConsoleScreenBufferInfo(
        hConsoleOutput: WinDef.HANDLE?,
        lpConsoleScreenBufferInfo: WinDef.CONSOLE_SCREEN_BUFFER_INFO?
    ): Boolean
}
