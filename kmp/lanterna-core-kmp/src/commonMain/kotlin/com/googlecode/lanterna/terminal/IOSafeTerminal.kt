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
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke

import java.util.concurrent.TimeUnit

/**
 * Interface extending Terminal that removes the IOException throw clause. You can for example use this instead of
 * Terminal if you use an implementation that doesn't throw any IOExceptions or if you wrap your terminal in an
 * IOSafeTerminalAdapter. Please note that readInput() still throws IOException when it is interrupted, in order to fit
 * better in with what normal terminal do when they are blocked on input and you interrupt them.
 * @author Martin
 */
 interface IOSafeTerminal:Terminal {
@get:Override
@set:Override
 var cursorPosition:TerminalPosition?
@get:Override
 val terminalSize:TerminalSize?
@Override
@JvmStatic  fun enterPrivateMode() 
@Override
@JvmStatic  fun exitPrivateMode() 
@Override
@JvmStatic  fun clearScreen() 
@Override
 fun setCursorPosition(x:Int, y:Int) 
@Override
 fun setCursorVisible(visible:Boolean) 
@Override
 fun putCharacter(c:Char) 
@Override
 fun putString(string:String?) 
@Override
 fun enableSGR(sgr:SGR?) 
@Override
 fun disableSGR(sgr:SGR?) 
@Override
@JvmStatic  fun resetColorAndSGR() 
@Override
 fun setForegroundColor(color:TextColor?) 
@Override
 fun setBackgroundColor(color:TextColor?) 
@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? 
@Override
@JvmStatic  fun bell() 
@Override
@JvmStatic  fun flush() 
@Override
 fun pollInput():KeyStroke? 
@Override
 fun readInput():KeyStroke? 
@Override
@JvmStatic  fun close() 
}
