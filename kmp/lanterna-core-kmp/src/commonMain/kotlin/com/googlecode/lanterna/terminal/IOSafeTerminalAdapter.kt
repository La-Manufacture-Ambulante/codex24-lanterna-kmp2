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
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * This class exposes methods for converting a terminal into an IOSafeTerminal. There are two options available, either
 * one that will convert any IOException to a RuntimeException (and re-throw it) or one that will silently swallow any
 * IOException (and return null in those cases the method has a non-void return type).
 * @author Martin
 */
 class IOSafeTerminalAdapter @SuppressWarnings("WeakerAccess")
 constructor(private val backend:Terminal?, internal val exceptionHandler:ExceptionHandler?):IOSafeTerminal {

 var cursorPosition:TerminalPosition?
@Override
get() {
try
{
return backend!!.getCursorPosition()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}
@Override
set(position) {
try
{
backend!!.setCursorPosition(position)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

 val terminalSize:TerminalSize?
@Override
get() {
try
{
return backend!!.getTerminalSize()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}
private interface ExceptionHandler {
 fun onException(e:IOException?) 
}

private class ConvertToRuntimeException:ExceptionHandler {
@Override
public override fun onException(e:IOException?) {
throw RuntimeException(e)
}
}

private class DoNothingAndOrReturnNull:ExceptionHandler {
@Override
public override fun onException(e:IOException?) {}
}

@Override
 fun enterPrivateMode() {
try
{
backend!!.enterPrivateMode()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun exitPrivateMode() {
try
{
backend!!.exitPrivateMode()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun clearScreen() {
try
{
backend!!.clearScreen()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setCursorPosition(x:Int, y:Int) {
try
{
backend!!.setCursorPosition(x, y)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setCursorVisible(visible:Boolean) {
try
{
backend!!.setCursorVisible(visible)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun putCharacter(c:Char) {
try
{
backend!!.putCharacter(c)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun putString(string:String?) {
try
{
backend!!.putString(string)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun newTextGraphics():TextGraphics? {
try
{
return backend!!.newTextGraphics()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}

@Override
 fun enableSGR(sgr:SGR?) {
try
{
backend!!.enableSGR(sgr)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun disableSGR(sgr:SGR?) {
try
{
backend!!.disableSGR(sgr)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun resetColorAndSGR() {
try
{
backend!!.resetColorAndSGR()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setForegroundColor(color:TextColor?) {
try
{
backend!!.setForegroundColor(color)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setBackgroundColor(color:TextColor?) {
try
{
backend!!.setBackgroundColor(color)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun addResizeListener(listener:TerminalResizeListener?) {
backend!!.addResizeListener(listener)
}

@Override
 fun removeResizeListener(listener:TerminalResizeListener?) {
backend!!.removeResizeListener(listener)
}

@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
try
{
return backend!!.enquireTerminal(timeout, timeoutUnit)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}

@Override
 fun bell() {
try
{
backend!!.bell()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun flush() {
try
{
backend!!.flush()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun close() {
try
{
backend!!.close()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun pollInput():KeyStroke? {
try
{
return backend!!.pollInput()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}

@Override
 fun readInput():KeyStroke? {
try
{
return backend!!.readInput()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

return null
}

/**
 * This class exposes methods for converting an extended terminal into an IOSafeExtendedTerminal.
 */
     class Extended(private val backend:ExtendedTerminal?, exceptionHandler:ExceptionHandler?):IOSafeTerminalAdapter(backend, exceptionHandler), IOSafeExtendedTerminal {

@Override
 fun setTerminalSize(columns:Int, rows:Int) {
try
{
backend!!.setTerminalSize(columns, rows)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setTitle(title:String?) {
try
{
backend!!.setTitle(title)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun pushTitle() {
try
{
backend!!.pushTitle()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun popTitle() {
try
{
backend!!.popTitle()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun iconify() {
try
{
backend!!.iconify()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun deiconify() {
try
{
backend!!.deiconify()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun maximize() {
try
{
backend!!.maximize()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun unmaximize() {
try
{
backend!!.unmaximize()
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun setMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?) {
try
{
backend!!.setMouseCaptureMode(mouseCaptureMode)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

@Override
 fun scrollLines(firstLine:Int, lastLine:Int, distance:Int) {
try
{
backend!!.scrollLines(firstLine, lastLine, distance)
}
catch (e:IOException) {
exceptionHandler!!.onException(e)
}

}

}

companion object {

/**
 * Creates a wrapper around a Terminal that exposes it as a IOSafeTerminal. If any IOExceptions occur, they will be
 * wrapped by a RuntimeException and re-thrown.
 * @param terminal Terminal to wrap
 * @return IOSafeTerminal wrapping the supplied terminal
 */
     fun createRuntimeExceptionConvertingAdapter(terminal:Terminal?):IOSafeTerminal {
if (terminal is ExtendedTerminal)
{ // also handle Runtime-type:
return createRuntimeExceptionConvertingAdapter(terminal as ExtendedTerminal?)
}
else
{
return IOSafeTerminalAdapter(terminal, ConvertToRuntimeException())
}
}

/**
 * Creates a wrapper around an ExtendedTerminal that exposes it as a IOSafeExtendedTerminal.
 * If any IOExceptions occur, they will be wrapped by a RuntimeException and re-thrown.
 * @param terminal Terminal to wrap
 * @return IOSafeTerminal wrapping the supplied terminal
 */
     fun createRuntimeExceptionConvertingAdapter(terminal:ExtendedTerminal?):IOSafeExtendedTerminal {
return IOSafeTerminalAdapter.Extended(terminal, ConvertToRuntimeException())
}

/**
 * Creates a wrapper around a Terminal that exposes it as a IOSafeTerminal. If any IOExceptions occur, they will be
 * silently ignored and for those method with a non-void return type, null will be returned.
 * @param terminal Terminal to wrap
 * @return IOSafeTerminal wrapping the supplied terminal
 */
     fun createDoNothingOnExceptionAdapter(terminal:Terminal?):IOSafeTerminal {
if (terminal is ExtendedTerminal)
{ // also handle Runtime-type:
return createDoNothingOnExceptionAdapter(terminal as ExtendedTerminal?)
}
else
{
return IOSafeTerminalAdapter(terminal, DoNothingAndOrReturnNull())
}
}

/**
 * Creates a wrapper around an ExtendedTerminal that exposes it as a IOSafeExtendedTerminal.
 * If any IOExceptions occur, they will be silently ignored and for those method with a
 * non-void return type, null will be returned.
 * @param terminal Terminal to wrap
 * @return IOSafeTerminal wrapping the supplied terminal
 */
     fun createDoNothingOnExceptionAdapter(terminal:ExtendedTerminal?):IOSafeExtendedTerminal {
return IOSafeTerminalAdapter.Extended(terminal, DoNothingAndOrReturnNull())
}
}
}
