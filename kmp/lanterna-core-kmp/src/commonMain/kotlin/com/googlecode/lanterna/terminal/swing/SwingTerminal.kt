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
package com.googlecode.lanterna.terminal.swing

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.terminal.MouseCaptureMode
import com.googlecode.lanterna.terminal.TerminalResizeListener

import javax.swing.*
import java.awt.*
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import java.awt.im.InputMethodRequests
import java.text.AttributedCharacterIterator
import java.util.concurrent.TimeUnit

/**
 * This class provides an Swing implementation of the [com.googlecode.lanterna.terminal.Terminal] interface that
 * is an embeddable component you can put into a Swing container. The class has static helper methods for opening a new
 * frame with a [SwingTerminal] as its content, similar to how the SwingTerminal used to work in earlier versions
 * of lanterna. This version supports private mode and non-private mode with a scrollback history. You can customize
 * many of the properties by supplying device configuration, font configuration and color configuration when you
 * construct the object.
 * @author martin
 */
@SuppressWarnings("serial")
 class SwingTerminal:JComponent, IOSafeTerminal {

private val terminalImplementation:SwingTerminalImplementation?
private val inputMethodRequests:TerminalInputMethodRequests?

/**
 * Returns the current font configuration. Note that it is immutable and cannot be changed.
 * @return This SwingTerminal's current font configuration
 */
     val fontConfiguration:SwingTerminalFontConfiguration?
get() {
return terminalImplementation!!.getFontConfiguration()
}

/**
 * Returns this terminal emulator's color configuration. Note that it is immutable and cannot be changed.
 * @return This [SwingTerminal]'s color configuration
 */
     val colorConfiguration:TerminalEmulatorColorConfiguration?
get() {
return terminalImplementation!!.getColorConfiguration()
}

/**
 * Returns this terminal emulator's device configuration. Note that it is immutable and cannot be changed.
 * @return This [SwingTerminal]'s device configuration
 */
     val deviceConfiguration:TerminalEmulatorDeviceConfiguration?
get() {
return terminalImplementation!!.getDeviceConfiguration()
}

/**
 * Overridden method from Swing's `JComponent` class that returns the preferred size of the terminal (in
 * pixels)
 * @return The terminal's preferred size in pixels
 */
     val preferredSize:Dimension?
@Override
@Synchronized get() {
return terminalImplementation!!.getPreferredSize()
}

 var cursorPosition:TerminalPosition?
@Override
get() {
return terminalImplementation!!.getCursorPosition()
}
@Override
set(position) {
terminalImplementation!!.setCursorPosition(position)
}

 val terminalSize:TerminalSize?
@Override
get() {
return terminalImplementation!!.getTerminalSize()
}

/**
 * Creates a new SwingTerminal with all the defaults set and no scroll controller connected.
 */
     constructor() : this(TerminalScrollController.Null()) {}


/**
 * Creates a new SwingTerminal with a particular scrolling controller that will be notified when the terminals
 * history size grows and will be called when this class needs to figure out the current scrolling position.
 * @param scrollController Controller for scrolling the terminal history
 */
    @SuppressWarnings("WeakerAccess")
 constructor(scrollController:TerminalScrollController?) : this(TerminalEmulatorDeviceConfiguration.getDefault(), 
SwingTerminalFontConfiguration.getDefault(), 
TerminalEmulatorColorConfiguration.getDefault(), 
scrollController) {}

/**
 * Creates a new SwingTerminal component using custom settings and no scroll controller.
 * @param deviceConfiguration Device configuration to use for this SwingTerminal
 * @param fontConfiguration Font configuration to use for this SwingTerminal
 * @param colorConfiguration Color configuration to use for this SwingTerminal
 */
     constructor(
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:SwingTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration) {}

/**
 * Creates a new SwingTerminal component using custom settings and a custom scroll controller. The scrolling
 * controller will be notified when the terminal's history size grows and will be called when this class needs to
 * figure out the current scrolling position.
 * @param deviceConfiguration Device configuration to use for this SwingTerminal
 * @param fontConfiguration Font configuration to use for this SwingTerminal
 * @param colorConfiguration Color configuration to use for this SwingTerminal
 * @param scrollController Controller to use for scrolling, the object passed in will be notified whenever the
 * scrollable area has changed
 */
     constructor(
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:SwingTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?, 
scrollController:TerminalScrollController?) : this(null, deviceConfiguration, fontConfiguration, colorConfiguration, scrollController) {}



/**
 * Creates a new SwingTerminal component using custom settings and a custom scroll controller. The scrolling
 * controller will be notified when the terminal's history size grows and will be called when this class needs to
 * figure out the current scrolling position.
 * @param initialTerminalSize Initial size of the terminal, which will be used when calculating the preferred size
 * of the component. If null, it will default to 80x25. If the AWT layout manager forces
 * the component to a different size, the value of this parameter won't have any meaning
 * @param deviceConfiguration Device configuration to use for this SwingTerminal
 * @param fontConfiguration Font configuration to use for this SwingTerminal
 * @param colorConfiguration Color configuration to use for this SwingTerminal
 * @param scrollController Controller to use for scrolling, the object passed in will be notified whenever the
 * scrollable area has changed
 */
    @JvmOverloads  constructor(
initialTerminalSize:TerminalSize?, 
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:SwingTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?, 
scrollController:TerminalScrollController? = TerminalScrollController.Null()) {
var deviceConfiguration = deviceConfiguration
var fontConfiguration = fontConfiguration
var colorConfiguration = colorConfiguration

 //Enforce valid values on the input parameters
        if (deviceConfiguration == null)
{
deviceConfiguration = TerminalEmulatorDeviceConfiguration.getDefault()
}
if (fontConfiguration == null)
{
fontConfiguration = SwingTerminalFontConfiguration.getDefault()
}
if (colorConfiguration == null)
{
colorConfiguration = TerminalEmulatorColorConfiguration.getDefault()
}

 // This will enable CJK and complex input systems
        enableInputMethods(true)

 // For some reason an InputMethodListener needs to be attached in order to start receiving IME events.
        addInputMethodListener(object:InputMethodListener() {
@Override
 fun inputMethodTextChanged(event:InputMethodEvent?) {}

@Override
 fun caretPositionChanged(event:InputMethodEvent?) {}
})

terminalImplementation = SwingTerminalImplementation(
this, 
fontConfiguration, 
initialTerminalSize, 
deviceConfiguration, 
colorConfiguration, 
scrollController)

inputMethodRequests = TerminalInputMethodRequests(this, terminalImplementation)
}

/**
 * Overridden method from Swing's `JComponent` class that is called by OS window system when the component
 * needs to be redrawn
 * @param componentGraphics `Graphics` object to use when drawing the component
 */
    @Override
@Synchronized protected fun paintComponent(componentGraphics:Graphics?) {
terminalImplementation!!.paintComponent(componentGraphics)
}

/**
 * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
 * input events to be processed as if they came from the user typing on the keyboard.
 * @param keyStroke Key stroke input event to put on the queue
 */
     fun addInput(keyStroke:KeyStroke?) {
terminalImplementation!!.addInput(keyStroke)
}

 fun setMouseCaptureMode(mouseCaptureMode:MouseCaptureMode?) {
terminalImplementation!!.setMouseCaptureMode(mouseCaptureMode)
}

@Override
 fun getInputMethodRequests():InputMethodRequests? {
return inputMethodRequests
}

@Override
protected fun processInputMethodEvent(e:InputMethodEvent) {
val iterator = e.getText()
for (i in 0 until e.getCommittedCharacterCount())
{
terminalImplementation!!.addInput(KeyStroke(iterator!!.current(), false, false))
iterator!!.next()
}
}

/**///////////////////////////////////////////////////////////////////////////// */
    // Terminal methods below here, just forward to the implementation

    @Override
@JvmStatic  fun enterPrivateMode() {
terminalImplementation!!.enterPrivateMode()
}

@Override
@JvmStatic  fun exitPrivateMode() {
terminalImplementation!!.exitPrivateMode()
}

@Override
@JvmStatic  fun clearScreen() {
terminalImplementation!!.clearScreen()
}

@Override
 fun setCursorPosition(x:Int, y:Int) {
terminalImplementation!!.setCursorPosition(x, y)
}

@Override
 fun setCursorVisible(visible:Boolean) {
terminalImplementation!!.setCursorVisible(visible)
}

@Override
 fun putCharacter(c:Char) {
terminalImplementation!!.putCharacter(c)
}

@Override
 fun putString(string:String?) {
terminalImplementation!!.putString(string)
}

@Override
 fun enableSGR(sgr:SGR?) {
terminalImplementation!!.enableSGR(sgr)
}

@Override
 fun disableSGR(sgr:SGR?) {
terminalImplementation!!.disableSGR(sgr)
}

@Override
@JvmStatic  fun resetColorAndSGR() {
terminalImplementation!!.resetColorAndSGR()
}

@Override
 fun setForegroundColor(color:TextColor?) {
terminalImplementation!!.setForegroundColor(color)
}

@Override
 fun setBackgroundColor(color:TextColor?) {
terminalImplementation!!.setBackgroundColor(color)
}

@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
return terminalImplementation!!.enquireTerminal(timeout, timeoutUnit)
}

@Override
@JvmStatic  fun bell() {
terminalImplementation!!.bell()
}

@Override
@JvmStatic  fun flush() {
terminalImplementation!!.flush()
}

@Override
@JvmStatic  fun close() {
terminalImplementation!!.close()
}

@Override
 fun pollInput():KeyStroke? {
return terminalImplementation!!.pollInput()
}

@Override
 fun readInput():KeyStroke? {
return terminalImplementation!!.readInput()
}

@Override
 fun newTextGraphics():TextGraphics? {
return terminalImplementation!!.newTextGraphics()
}

@Override
 fun addResizeListener(listener:TerminalResizeListener?) {
terminalImplementation!!.addResizeListener(listener)
}

@Override
 fun removeResizeListener(listener:TerminalResizeListener?) {
terminalImplementation!!.removeResizeListener(listener)
}
}/**
 * Creates a new SwingTerminal component using custom settings and no scroll controller.
 * @param initialTerminalSize Initial size of the terminal, which will be used when calculating the preferred size
 * of the component. If null, it will default to 80x25. If the AWT layout manager forces
 * the component to a different size, the value of this parameter won't have any meaning
 * @param deviceConfiguration Device configuration to use for this SwingTerminal
 * @param fontConfiguration Font configuration to use for this SwingTerminal
 * @param colorConfiguration Color configuration to use for this SwingTerminal
 */
