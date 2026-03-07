package com.googlecode.lanterna.terminal.win32

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.ArrayList

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.input.BasicCharacterPattern
import com.googlecode.lanterna.input.CharacterPattern
import com.googlecode.lanterna.input.KeyDecodingProfile
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.win32.WinDef.CONSOLE_SCREEN_BUFFER_INFO
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal
import com.sun.jna.ptr.IntByReference

 class WindowsTerminal @Throws(IOException::class)
@JvmOverloads  constructor(terminalInput:InputStream? = CONSOLE_INPUT, terminalOutput:OutputStream? = CONSOLE_OUTPUT, terminalCharset:Charset? = CONSOLE_CHARSET, terminalCtrlCBehaviour:CtrlCBehaviour? = CtrlCBehaviour.CTRL_C_KILLS_APPLICATION):UnixLikeTerminal(CONSOLE_INPUT, CONSOLE_OUTPUT, CONSOLE_CHARSET, terminalCtrlCBehaviour) {

private var settings:IntArray? = null

protected// handle Key Code 13 as ENTER
 // handle everything else as per default
 val defaultKeyDecodingProfile:KeyDecodingProfile?
@Override
get() {
val keyDecodingProfile = ArrayList<CharacterPattern?>()
keyDecodingProfile.add(BasicCharacterPattern(KeyStroke(KeyType.ENTER), '\r'))
keyDecodingProfile.addAll(super.getDefaultKeyDecodingProfile().getPatterns())
return { keyDecodingProfile }
}

 val cursorPosition:TerminalPosition
get() {
val screenBufferInfo = CONSOLE_SCREEN_BUFFER_INFO()
Wincon.INSTANCE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT.getHandle(), screenBufferInfo)
val column = screenBufferInfo.dwCursorPosition.X - screenBufferInfo.srWindow.Left
val row = screenBufferInfo.dwCursorPosition.Y - screenBufferInfo.srWindow.Top
return TerminalPosition(column, row)
}

private val consoleInputMode:Int
get() {
val lpMode = IntByReference()
Wincon.INSTANCE.GetConsoleMode(CONSOLE_INPUT.getHandle(), lpMode)
return lpMode.getValue()
}

private val consoleOutputMode:Int
get() {
val lpMode = IntByReference()
Wincon.INSTANCE.GetConsoleMode(CONSOLE_OUTPUT.getHandle(), lpMode)
return lpMode.getValue()
}

init{

 // handle resize events
		CONSOLE_INPUT.onWindowBufferSizeEvent({ evt-> onResized(evt!!.dwSize.X, evt!!.dwSize.Y) })
}

@Override
@Throws(IOException::class)
@JvmStatic protected fun acquire() {
super.acquire()

var terminalOutputMode = consoleOutputMode
terminalOutputMode = terminalOutputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_PROCESSING
terminalOutputMode = terminalOutputMode or Wincon.DISABLE_NEWLINE_AUTO_RETURN
Wincon.INSTANCE.SetConsoleMode(CONSOLE_OUTPUT.getHandle(), terminalOutputMode)

var terminalInputMode = consoleInputMode
terminalInputMode = terminalInputMode or Wincon.ENABLE_MOUSE_INPUT
terminalInputMode = terminalInputMode or Wincon.ENABLE_WINDOW_INPUT
terminalInputMode = terminalInputMode or Wincon.ENABLE_VIRTUAL_TERMINAL_INPUT
Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), terminalInputMode)
}

@Override
@JvmStatic  fun saveTerminalSettings() {
settings = intArrayOf(consoleInputMode, consoleOutputMode)
}

@Override
@JvmStatic  fun restoreTerminalSettings() {
if (settings != null)
{
Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), settings!![0])
Wincon.INSTANCE.SetConsoleMode(CONSOLE_OUTPUT.getHandle(), settings!![1])
}
}

@Override
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
Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
}

@Override
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
Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
}

@Override
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
Wincon.INSTANCE.SetConsoleMode(CONSOLE_INPUT.getHandle(), mode)
}

@Override
protected fun findTerminalSize():TerminalSize {
val screenBufferInfo = CONSOLE_SCREEN_BUFFER_INFO()
Wincon.INSTANCE.GetConsoleScreenBufferInfo(CONSOLE_OUTPUT.getHandle(), screenBufferInfo)
val columns = screenBufferInfo.srWindow.Right - screenBufferInfo.srWindow.Left + 1
val rows = screenBufferInfo.srWindow.Bottom - screenBufferInfo.srWindow.Top + 1
return TerminalSize(columns, rows)
}

@Override
 fun registerTerminalResizeListener(runnable:Runnable?) {
 // ignore
	}

companion object {

private val CONSOLE_CHARSET = StandardCharsets.UTF_8
private val CONSOLE_INPUT = WindowsConsoleInputStream(CONSOLE_CHARSET)
private val CONSOLE_OUTPUT = WindowsConsoleOutputStream(CONSOLE_CHARSET)
}
}
