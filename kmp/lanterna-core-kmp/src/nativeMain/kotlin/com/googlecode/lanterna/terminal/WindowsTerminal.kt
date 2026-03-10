package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Terminal implementation for the regular Windows cmd.exe terminal emulator, using native invocations through jna to
 * interact with it.
 */
 class WindowsTerminal @Throws(IOException::class)
@JvmOverloads  constructor(
terminalInput:InputStream? = System.`in`, 
terminalOutput:OutputStream? = System.out, 
terminalCharset:Charset? = Charset.defaultCharset(), 
terminalCtrlCBehaviour:CtrlCBehaviour? = CtrlCBehaviour.CTRL_C_KILLS_APPLICATION):UnixLikeTerminal(terminalInput, terminalOutput, terminalCharset, terminalCtrlCBehaviour) {

private var savedTerminalInputMode:Integer? = null
private var savedTerminalOutputMode:Integer? = null

 val cursorPosition:TerminalPosition
@Synchronized get() {
val screenBufferInfo = WinDef.CONSOLE_SCREEN_BUFFER_INFO()
WINDOWS_CONSOLE!!.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT_HANDLE, screenBufferInfo)
val column = screenBufferInfo.dwCursorPosition.X - screenBufferInfo.srWindow.Left
val row = screenBufferInfo.dwCursorPosition.Y - screenBufferInfo.srWindow.Top
return TerminalPosition(column, row)
}

private val consoleInputMode:Int
get() {
val lpMode = IntByReference()
WINDOWS_CONSOLE!!.GetConsoleMode(CONSOLE_INPUT_HANDLE, lpMode)
return lpMode.getValue()
}

private val consoleOutputMode:Int
get() {
val lpMode = IntByReference()
WINDOWS_CONSOLE!!.GetConsoleMode(CONSOLE_OUTPUT_HANDLE, lpMode)
return lpMode.getValue()
}

@Override
@Throws(IOException::class)
@JvmStatic protected fun acquire() {
super.acquire()
var terminalOutputMode = consoleOutputMode
terminalOutputMode = terminalOutputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_PROCESSING
terminalOutputMode = terminalOutputMode or Wincon.DISABLE_NEWLINE_AUTO_RETURN
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_OUTPUT_HANDLE, terminalOutputMode)
}

@Override
@Synchronized @Throws(IOException::class)
@JvmStatic  fun saveTerminalSettings() {
this.savedTerminalInputMode = consoleInputMode
this.savedTerminalOutputMode = consoleOutputMode
}

@Override
@Synchronized @Throws(IOException::class)
@JvmStatic  fun restoreTerminalSettings() {
if (savedTerminalInputMode != null)
{
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_INPUT_HANDLE, savedTerminalInputMode)
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_OUTPUT_HANDLE, savedTerminalOutputMode)
}
}

@Override
@Synchronized @Throws(IOException::class)
 fun keyEchoEnabled(enabled:Boolean) {
var mode = consoleInputMode
if (enabled)
{
mode = mode or Wincon.ENABLE_ECHO_INPUT
}
else
{
mode = mode and Wincon.ENABLE_ECHO_INPUT.inv()
}
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
}

@Override
@Synchronized @Throws(IOException::class)
 fun canonicalMode(enabled:Boolean) {
var mode = consoleInputMode
if (enabled)
{
mode = mode or Wincon.ENABLE_LINE_INPUT
}
else
{
mode = mode and Wincon.ENABLE_LINE_INPUT.inv()
}
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
}

@Override
@Synchronized @Throws(IOException::class)
 fun keyStrokeSignalsEnabled(enabled:Boolean) {
var mode = consoleInputMode
if (enabled)
{
mode = mode or Wincon.ENABLE_PROCESSED_INPUT
}
else
{
mode = mode and Wincon.ENABLE_PROCESSED_INPUT.inv()
}
WINDOWS_CONSOLE!!.SetConsoleMode(CONSOLE_INPUT_HANDLE, mode)
}


@Override
@Throws(IOException::class)
protected fun findTerminalSize():TerminalSize {
val screenBufferInfo = WinDef.CONSOLE_SCREEN_BUFFER_INFO()
WINDOWS_CONSOLE!!.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT_HANDLE, screenBufferInfo)
val columns = screenBufferInfo.srWindow.Right - screenBufferInfo.srWindow.Left + 1
val rows = screenBufferInfo.srWindow.Bottom - screenBufferInfo.srWindow.Top + 1
return TerminalSize(columns, rows)
}

@Override
@Throws(IOException::class)
 fun registerTerminalResizeListener(runnable:Runnable?) {
 // Not implemented yet
    }

companion object {

private val WINDOWS_CONSOLE = Native.loadLibrary("kernel32", Wincon::class.java, W32APIOptions.UNICODE_OPTIONS) as Wincon?
private val CONSOLE_INPUT_HANDLE = WINDOWS_CONSOLE!!.GetStdHandle(Wincon.STD_INPUT_HANDLE)
private val CONSOLE_OUTPUT_HANDLE = WINDOWS_CONSOLE!!.GetStdHandle(Wincon.STD_OUTPUT_HANDLE)
}
}
