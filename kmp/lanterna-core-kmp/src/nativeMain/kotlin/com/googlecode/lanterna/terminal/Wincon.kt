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
internal interface Wincon:StdCallLibrary {

 fun GetStdHandle(var1:Int):WinDef.HANDLE? 
 fun GetConsoleMode(var1:WinDef.HANDLE?, var2:IntByReference?):Boolean 
 fun SetConsoleMode(var1:WinDef.HANDLE?, var2:Int):Boolean 
 fun GetConsoleScreenBufferInfo(hConsoleOutput:WinDef.HANDLE?, lpConsoleScreenBufferInfo:WinDef.CONSOLE_SCREEN_BUFFER_INFO?):Boolean 

companion object {
 val STD_INPUT_HANDLE = -10
 val STD_OUTPUT_HANDLE = -11

 // SetConsoleMode input values
     val ENABLE_PROCESSED_INPUT = 1
 val ENABLE_LINE_INPUT = 2
 val ENABLE_ECHO_INPUT = 4

 // SetConsoleMode screen buffer values
     val ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4
 val DISABLE_NEWLINE_AUTO_RETURN = 8
}
}
