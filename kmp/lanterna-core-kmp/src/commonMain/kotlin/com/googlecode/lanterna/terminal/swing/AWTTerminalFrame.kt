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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.TerminalResizeListener

import java.awt.*
import java.util.Arrays
import java.util.EnumSet
import java.util.concurrent.TimeUnit

/**
 * This class is similar to what SwingTerminal used to be before Lanterna 3.0; a Frame that contains a terminal
 * emulator. In Lanterna 3, this class is just an AWT Frame containing a [AWTTerminal] component, but it also
 * implements the [com.googlecode.lanterna.terminal.Terminal] interface and delegates all calls to the internal
 * [AWTTerminal]. You can tweak the class a bit to have special behaviours when exiting private mode or when the
 * user presses ESC key.
 * 
 * 
 * Please note that this is the AWT version and there is a Swing counterpart: [SwingTerminalFrame]
 * @see AWTTerminal
 * 
 * @see SwingTerminalFrame
 * 
 * @author martin
 */
@SuppressWarnings("serial")
 class AWTTerminalFrame private constructor(title:String?, /**
 * Returns the wrapped AWTTerminal which is holding the actual terminal content. This can be useful if you want to
 * add custom AWT listeners to it, etc.
 * @return The inner AWTTerminal
 */
     val awtTerminal:AWTTerminal?, vararg autoCloseTrigger:TerminalEmulatorAutoCloseTrigger?):Frame(if (title != null) title else "AWTTerminalFrame"), IOSafeTerminal {
private val autoCloseTriggers:EnumSet<TerminalEmulatorAutoCloseTrigger?>?

private var disposed:Boolean = false

/**
 * Returns the current font configuration. Note that it is immutable and cannot be changed.
 * @return This [AWTTerminalFrame]'s current font configuration
 */
     val fontConfiguration:AWTTerminalFontConfiguration?
get() {
return awtTerminal!!.getFontConfiguration()
}

/**
 * Returns this terminal emulator's color configuration. Note that it is immutable and cannot be changed.
 * @return This [AWTTerminalFrame]'s color configuration
 */
     val colorConfiguration:TerminalEmulatorColorConfiguration?
get() {
return awtTerminal!!.getColorConfiguration()
}

/**
 * Returns this terminal emulator's device configuration. Note that it is immutable and cannot be changed.
 * @return This [AWTTerminalFrame]'s device configuration
 */
     val deviceConfiguration:TerminalEmulatorDeviceConfiguration?
get() {
return awtTerminal!!.getDeviceConfiguration()
}

/**
 * Returns the auto-close triggers used by the AWTTerminalFrame
 * @return Current auto-close trigger
 */
     val autoCloseTrigger:Set<TerminalEmulatorAutoCloseTrigger?>?
get() {
return EnumSet.copyOf(autoCloseTriggers)
}

 var cursorPosition:TerminalPosition?
@Override
get() {
return awtTerminal!!.getCursorPosition()
}
@Override
set(position) {
awtTerminal!!.setCursorPosition(position)
}

 val terminalSize:TerminalSize?
@Override
get() {
return awtTerminal!!.getTerminalSize()
}

/**
 * Creates a new AWTTerminalFrame with an optional list of auto-close triggers
 * @param autoCloseTriggers What to trigger automatic disposal of the Frame
 */
    @SuppressWarnings("SameParameterValue", "WeakerAccess")
 constructor(vararg autoCloseTriggers:TerminalEmulatorAutoCloseTrigger?) : this("AwtTerminalFrame", *autoCloseTriggers) {}

/**
 * Creates a new AWTTerminalFrame with a given window title and an optional list of auto-close triggers
 * @param title Title to use for the window
 * @param autoCloseTriggers What to trigger automatic disposal of the Frame
 */
    @SuppressWarnings("WeakerAccess")
@Throws(HeadlessException::class)
 constructor(title:String?, vararg autoCloseTriggers:TerminalEmulatorAutoCloseTrigger?) : this(title, AWTTerminal(), *autoCloseTriggers) {}

/**
 * Creates a new AWTTerminalFrame using a specified title and a series of AWT terminal configuration objects
 * @param title What title to use for the window
 * @param deviceConfiguration Device configuration for the embedded AWTTerminal
 * @param fontConfiguration Font configuration for the embedded AWTTerminal
 * @param colorConfiguration Color configuration for the embedded AWTTerminal
 * @param autoCloseTriggers What to trigger automatic disposal of the Frame
 */
     constructor(title:String?, 
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:AWTTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?, 
vararg autoCloseTriggers:TerminalEmulatorAutoCloseTrigger?) : this(title, null, deviceConfiguration, fontConfiguration, colorConfiguration, *autoCloseTriggers) {}

/**
 * Creates a new AWTTerminalFrame using a specified title and a series of AWT terminal configuration objects
 * @param title What title to use for the window
 * @param terminalSize Initial size of the terminal, in rows and columns. If null, it will default to 80x25.
 * @param deviceConfiguration Device configuration for the embedded AWTTerminal
 * @param fontConfiguration Font configuration for the embedded AWTTerminal
 * @param colorConfiguration Color configuration for the embedded AWTTerminal
 * @param autoCloseTriggers What to trigger automatic disposal of the Frame
 */
     constructor(title:String?, 
terminalSize:TerminalSize?, 
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:AWTTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?, 
vararg autoCloseTriggers:TerminalEmulatorAutoCloseTrigger?) : this(title, 
AWTTerminal(terminalSize, deviceConfiguration, fontConfiguration, colorConfiguration), 
*autoCloseTriggers) {}

init{
this.autoCloseTriggers = EnumSet.copyOf(Arrays.asList(autoCloseTrigger))
this.disposed = false

setLayout(BorderLayout())
add(awtTerminal, BorderLayout.CENTER)
setBackground(Color.BLACK) //This will reduce white flicker when resizing the window
pack()

 //Put input focus on the terminal component by default
        awtTerminal!!.requestFocusInWindow()
}

 fun addAutoCloseTrigger(autoCloseTrigger:TerminalEmulatorAutoCloseTrigger?) {
autoCloseTriggers!!.add(autoCloseTrigger)
}

@Override
 fun dispose() {
super.dispose()
disposed = true
}

@Override
 fun close() {
dispose()
}

/**
 * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
 * input events to be processed as if they came from the user typing on the keyboard.
 * @param keyStroke Key stroke input event to put on the queue
 */
     fun addInput(keyStroke:KeyStroke?) {
awtTerminal!!.addInput(keyStroke)
}

/**//////// */
    // Delegate all Terminal interface implementations to AWTTerminal
    /**//////// */
    @Override
 fun pollInput():KeyStroke? {
if (disposed)
{
return KeyStroke(KeyType.EOF)
}
val keyStroke = awtTerminal!!.pollInput()
if ((autoCloseTriggers!!.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_ESCAPE) && 
keyStroke != null && 
keyStroke!!.getKeyType() === KeyType.ESCAPE))
{
dispose()
}
return keyStroke
}

@Override
 fun readInput():KeyStroke? {
return awtTerminal!!.readInput()
}

@Override
 fun enterPrivateMode() {
awtTerminal!!.enterPrivateMode()
}

@Override
 fun exitPrivateMode() {
awtTerminal!!.exitPrivateMode()
if (autoCloseTriggers!!.contains(TerminalEmulatorAutoCloseTrigger.CLOSE_ON_EXIT_PRIVATE_MODE))
{
dispose()
}
}

@Override
 fun clearScreen() {
awtTerminal!!.clearScreen()
}

@Override
 fun setCursorPosition(x:Int, y:Int) {
awtTerminal!!.setCursorPosition(x, y)
}

@Override
 fun setCursorVisible(visible:Boolean) {
awtTerminal!!.setCursorVisible(visible)
}

@Override
 fun putCharacter(c:Char) {
awtTerminal!!.putCharacter(c)
}

@Override
 fun putString(string:String?) {
awtTerminal!!.putString(string)
}

@Override
 fun newTextGraphics():TextGraphics? {
return awtTerminal!!.newTextGraphics()
}

@Override
 fun enableSGR(sgr:SGR?) {
awtTerminal!!.enableSGR(sgr)
}

@Override
 fun disableSGR(sgr:SGR?) {
awtTerminal!!.disableSGR(sgr)
}

@Override
 fun resetColorAndSGR() {
awtTerminal!!.resetColorAndSGR()
}

@Override
 fun setForegroundColor(color:TextColor?) {
awtTerminal!!.setForegroundColor(color)
}

@Override
 fun setBackgroundColor(color:TextColor?) {
awtTerminal!!.setBackgroundColor(color)
}

@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
return awtTerminal!!.enquireTerminal(timeout, timeoutUnit)
}

@Override
 fun bell() {
awtTerminal!!.bell()
}

@Override
 fun flush() {
awtTerminal!!.flush()
}

@Override
 fun addResizeListener(listener:TerminalResizeListener?) {
awtTerminal!!.addResizeListener(listener)
}

@Override
 fun removeResizeListener(listener:TerminalResizeListener?) {
awtTerminal!!.removeResizeListener(listener)
}
}
