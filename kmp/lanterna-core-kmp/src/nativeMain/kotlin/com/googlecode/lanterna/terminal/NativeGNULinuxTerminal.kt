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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.sun.jna.Native

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

import com.googlecode.lanterna.terminal.PosixLibC.*

/**
 * Terminal implementation that uses native libraries
 */
 class NativeGNULinuxTerminal @Throws(IOException::class)
@JvmOverloads  constructor(
terminalInput:InputStream? = System.`in`, 
terminalOutput:OutputStream? = System.out, 
terminalCharset:Charset? = Charset.defaultCharset(), 
terminalCtrlCBehaviour:CtrlCBehaviour? = CtrlCBehaviour.CTRL_C_KILLS_APPLICATION):UnixLikeTerminal(terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {

private val libc:PosixLibC?
private var savedTerminalState:PosixLibC.termios? = null

private val terminalState:PosixLibC.termios
get() {
val termios = PosixLibC.termios()
libc!!.tcgetattr(STDIN_FILENO, termios)
return termios
}

init{


this.libc = Native.loadLibrary("c", PosixLibC::class.java) as PosixLibC
this.savedTerminalState = null
}

@Throws(IOException::class)
@JvmStatic  fun saveTerminalSettings() {
savedTerminalState = terminalState
}

@Throws(IOException::class)
@JvmStatic  fun restoreTerminalSettings() {
if (savedTerminalState != null)
{
libc!!.tcsetattr(STDIN_FILENO, TCSANOW, savedTerminalState)
}
}

@Throws(IOException::class)
 fun keyEchoEnabled(b:Boolean) {
val state = terminalState
if (b)
{
state.c_lflag = state.c_lflag or ECHO
}
else
{
state.c_lflag = state.c_lflag and ECHO.inv()
}
libc!!.tcsetattr(STDIN_FILENO, TCSANOW, state)
}

@Throws(IOException::class)
 fun canonicalMode(b:Boolean) {
val state = terminalState
if (b)
{
state.c_lflag = state.c_lflag or ICANON
}
else
{
state.c_lflag = state.c_lflag and ICANON.inv()
}
libc!!.tcsetattr(STDIN_FILENO, TCSANOW, state)
}

@Throws(IOException::class)
 fun keyStrokeSignalsEnabled(b:Boolean) {
val state = terminalState
if (b)
{
state.c_lflag = state.c_lflag or ISIG
}
else
{
state.c_lflag = state.c_lflag and ISIG.inv()
}
libc!!.tcsetattr(STDIN_FILENO, TCSANOW, state)
}

@Throws(IOException::class)
 fun registerTerminalResizeListener(runnable:Runnable?) {
libc!!.signal(SIGWINCH, object:sig_t() {
@Synchronized  fun invoke(signal:Int) {
runnable!!.run()
}
})
}

@Override
@Throws(IOException::class)
protected fun findTerminalSize():TerminalSize {
val winsize = winsize()
libc!!.ioctl(PosixLibC.STDOUT_FILENO, PosixLibC.TIOCGWINSZ, winsize)
return TerminalSize(winsize.ws_col, winsize.ws_row)
}
}
