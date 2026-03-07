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
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.terminal.IOSafeTerminal
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.TerminalResizeListener

import java.awt.*
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener
import java.util.concurrent.TimeUnit
import javax.swing.*

/**
 * This is a Swing JComponent that carries a [SwingTerminal] with a scrollbar, effectively implementing a
 * pseudo-terminal with scrollback history. You can choose the same parameters are for [SwingTerminal], they are
 * forwarded, this class mostly deals with linking the [SwingTerminal] with the scrollbar and having them update
 * each other.
 * @author Martin
 */
@SuppressWarnings("serial")
 class ScrollingSwingTerminal/**
 * Creates a new `ScrollingSwingTerminal` with customizable settings.
 * @param deviceConfiguration How to configure the terminal virtual device
 * @param fontConfiguration What kind of fonts to use
 * @param colorConfiguration Which color schema to use for ANSI colors
 */
     @SuppressWarnings("SameParameterValue", "WeakerAccess")
 constructor(
deviceConfiguration:TerminalEmulatorDeviceConfiguration?, 
fontConfiguration:SwingTerminalFontConfiguration?, 
colorConfiguration:TerminalEmulatorColorConfiguration?):JComponent(), IOSafeTerminal {

private val swingTerminal:SwingTerminal?
private val scrollBar:JScrollBar?

 // Used to prevent unnecessary repaints (the component is re-adjusting the scrollbar as part of the repaint
    // operation, we don't need the scrollbar listener to trigger another repaint of the terminal when that happens
    @Volatile private var scrollModelUpdateBySystem:Boolean = false

 var cursorPosition:TerminalPosition?
@Override
get() {
return swingTerminal!!.getCursorPosition()
}
@Override
set(position) {
swingTerminal!!.setCursorPosition(position)
}

 val terminalSize:TerminalSize?
@Override
get() {
return swingTerminal!!.getTerminalSize()
}

/**
 * Creates a new `ScrollingSwingTerminal` with all default options
 */
     constructor() : this(TerminalEmulatorDeviceConfiguration.getDefault(), 
SwingTerminalFontConfiguration.getDefault(), 
TerminalEmulatorColorConfiguration.getDefault()) {}

init{

this.scrollBar = JScrollBar(JScrollBar.VERTICAL)
this.swingTerminal = SwingTerminal(
deviceConfiguration, 
fontConfiguration, 
colorConfiguration, 
ScrollController())

setLayout(BorderLayout())
add(swingTerminal, BorderLayout.CENTER)
add(scrollBar, BorderLayout.EAST)
this.scrollBar!!.setMinimum(0)
this.scrollBar!!.setMaximum(20)
this.scrollBar!!.setValue(0)
this.scrollBar!!.setVisibleAmount(20)
this.scrollBar!!.addAdjustmentListener(ScrollbarListener())
this.scrollModelUpdateBySystem = false
}

private inner class ScrollController:TerminalScrollController {
@get:Override
 var scrollingOffset:Int = 0
private set

@Override
 fun updateModel(totalSize:Int, screenHeight:Int) {
if (!SwingUtilities.isEventDispatchThread())
{
SwingUtilities.invokeLater({ updateModel(totalSize, screenHeight) })
return 
}
try
{
scrollModelUpdateBySystem = true
var value = scrollBar!!.getValue()
var maximum = scrollBar!!.getMaximum()
var visibleAmount = scrollBar!!.getVisibleAmount()

if (maximum != totalSize)
{
val lastMaximum = maximum
maximum = if (totalSize > screenHeight) totalSize else screenHeight
if ((lastMaximum < maximum && lastMaximum - visibleAmount - value == 0))
{
value = scrollBar!!.getValue() + (maximum - lastMaximum)
}
}
if (value + screenHeight > maximum)
{
value = maximum - screenHeight
}
if (visibleAmount != screenHeight)
{
if (visibleAmount > screenHeight)
{
value += visibleAmount - screenHeight
}
visibleAmount = screenHeight
}
if (value > maximum - visibleAmount)
{
value = maximum - visibleAmount
}
if (value < 0)
{
value = 0
}

this.scrollingOffset = value

if (scrollBar!!.getMaximum() !== maximum)
{
scrollBar!!.setMaximum(maximum)
}
if (scrollBar!!.getVisibleAmount() !== visibleAmount)
{
scrollBar!!.setVisibleAmount(visibleAmount)
}
if (scrollBar!!.getValue() !== value)
{
scrollBar!!.setValue(value)
}
}

finally
{
scrollModelUpdateBySystem = false
}
}
}

private inner class ScrollbarListener:AdjustmentListener {
@Override
@Synchronized  fun adjustmentValueChanged(e:AdjustmentEvent?) {
if (!scrollModelUpdateBySystem)
{
 // Only repaint if this was the user adjusting the scrollbar
                swingTerminal!!.repaint()
}
}
}

/**
 * Takes a KeyStroke and puts it on the input queue of the terminal emulator. This way you can insert synthetic
 * input events to be processed as if they came from the user typing on the keyboard.
 * @param keyStroke Key stroke input event to put on the queue
 */
     fun addInput(keyStroke:KeyStroke?) {
swingTerminal!!.addInput(keyStroke)
}

/**//////// */
    // Delegate all Terminal interface implementations to SwingTerminal
    /**//////// */
    @Override
 fun pollInput():KeyStroke? {
return swingTerminal!!.pollInput()
}

@Override
 fun readInput():KeyStroke? {
return swingTerminal!!.readInput()
}

@Override
@JvmStatic  fun enterPrivateMode() {
swingTerminal!!.enterPrivateMode()
}

@Override
@JvmStatic  fun exitPrivateMode() {
swingTerminal!!.exitPrivateMode()
}

@Override
@JvmStatic  fun clearScreen() {
swingTerminal!!.clearScreen()
}

@Override
 fun setCursorPosition(x:Int, y:Int) {
swingTerminal!!.setCursorPosition(x, y)
}

@Override
 fun setCursorVisible(visible:Boolean) {
swingTerminal!!.setCursorVisible(visible)
}

@Override
 fun putCharacter(c:Char) {
swingTerminal!!.putCharacter(c)
}

@Override
 fun putString(string:String?) {
swingTerminal!!.putString(string)
}

@Override
 fun newTextGraphics():TextGraphics? {
return swingTerminal!!.newTextGraphics()
}

@Override
 fun enableSGR(sgr:SGR?) {
swingTerminal!!.enableSGR(sgr)
}

@Override
 fun disableSGR(sgr:SGR?) {
swingTerminal!!.disableSGR(sgr)
}

@Override
@JvmStatic  fun resetColorAndSGR() {
swingTerminal!!.resetColorAndSGR()
}

@Override
 fun setForegroundColor(color:TextColor?) {
swingTerminal!!.setForegroundColor(color)
}

@Override
 fun setBackgroundColor(color:TextColor?) {
swingTerminal!!.setBackgroundColor(color)
}

@Override
 fun enquireTerminal(timeout:Int, timeoutUnit:TimeUnit?):ByteArray? {
return swingTerminal!!.enquireTerminal(timeout, timeoutUnit)
}

@Override
@JvmStatic  fun bell() {
swingTerminal!!.bell()
}

@Override
@JvmStatic  fun flush() {
swingTerminal!!.flush()
}

@Override
@JvmStatic  fun close() {
swingTerminal!!.close()
}

@Override
 fun addResizeListener(listener:TerminalResizeListener?) {
swingTerminal!!.addResizeListener(listener)
}

@Override
 fun removeResizeListener(listener:TerminalResizeListener?) {
swingTerminal!!.removeResizeListener(listener)
}
}
