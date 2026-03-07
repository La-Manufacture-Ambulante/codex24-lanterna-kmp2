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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.Screen

import java.io.EOFException
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This abstract implementation of TextGUI contains some basic management of the underlying Screen and other common code
 * that can be shared between different implementations.
 * @author Martin
 */
abstract class AbstractTextGUI/**
 * Constructor for `AbstractTextGUI` that requires a `Screen` and a factory for creating the GUI thread
 * @param textGUIThreadFactory Factory class to use for creating the `TextGUIThread` class
 * @param screen What underlying `Screen` to use for this text GUI
 */
     protected constructor(textGUIThreadFactory:TextGUIThreadFactory?, @get:Override
 val screen:Screen?):TextGUI {
private val listeners:List<Listener?>?
/**
 * Checks if blocking I/O is enabled or not
 * @return `true` if blocking I/O is enabled, otherwise `false`
 */
    /**
 * Enables blocking I/O, causing calls to `readKeyStroke()` to block until there is input available. Notice
 * that you can still poll for input using `pollInput()`.
 * @param blockingIO Set this to `true` if blocking I/O should be enabled, otherwise `false`
 */
     var isBlockingIO:Boolean = false
private var dirty:Boolean = false
@get:Override
 val guiThread:TextGUIThread?
private var guiTheme:Theme? = null

 var theme:Theme?
@Override
get() {
return guiTheme
}
@Override
set(theme) {
if (theme != null)
{
this.guiTheme = theme
}
}

 val isPendingUpdate:Boolean
@Override
get() {
return screen!!.doResizeIfNecessary() != null || dirty
}

/**
 * Top-level method for drilling in to the GUI and figuring out, in global coordinates, where to place the text
 * cursor on the screen at this time.
 * @return Where to place the text cursor, or `null` if the cursor should be hidden
 */
    protected abstract val cursorPosition:TerminalPosition?

init{
if (screen == null)
{
throw IllegalArgumentException("Creating a TextGUI requires an underlying Screen")
}
this.listeners = CopyOnWriteArrayList()
this.isBlockingIO = false
this.dirty = false
this.guiTheme = LanternaThemes.getDefaultTheme()
this.guiThread = textGUIThreadFactory!!.createTextGUIThread(this)
}

/**
 * Reads one key from the input queue, blocking or non-blocking depending on if blocking I/O has been enabled. To
 * enable blocking I/O (disabled by default), use `setBlockingIO(true)`.
 * @return One piece of user input as a `KeyStroke` or `null` if blocking I/O is disabled and there was
 * no input waiting
 * @throws IOException In case of an I/O error while reading input
 */
    @Throws(IOException::class)
protected fun readKeyStroke():KeyStroke? {
return if (isBlockingIO) screen!!.readInput() else pollInput()
}

/**
 * Polls the underlying input queue for user input, returning either a `KeyStroke` or `null`
 * @return `KeyStroke` representing the user input or `null` if there was none
 * @throws IOException In case of an I/O error while reading input
 */
    @Throws(IOException::class)
protected fun pollInput():KeyStroke? {
return screen!!.pollInput()
}

@Override
@Synchronized @Throws(IOException::class)
 fun processInput():Boolean {
var gotInput = false
var keyStroke = readKeyStroke()
if (keyStroke != null)
{
gotInput = true
do
{
if (keyStroke!!.getKeyType() === KeyType.EOF)
{
throw EOFException()
}
var handled = handleInput(keyStroke)
if (!handled)
{
handled = fireUnhandledKeyStroke(keyStroke)
}
dirty = handled || dirty
keyStroke = pollInput()
}
while (keyStroke != null)
}
return gotInput
}

@Override
@Synchronized @Throws(IOException::class)
 fun updateScreen() {
screen!!.doResizeIfNecessary()
drawGUI(DefaultTextGUIGraphics(this, screen!!.newTextGraphics()))
screen!!.setCursorPosition(cursorPosition)
screen!!.refresh()
dirty = false
}

@Override
 fun addListener(listener:Listener?) {
listeners!!.add(listener)
}

@Override
 fun removeListener(listener:Listener?) {
listeners!!.remove(listener)
}

/**
 * This method should be called when there was user input that wasn't handled by the GUI. It will fire the
 * `onUnhandledKeyStroke(..)` method on any registered listener.
 * @param keyStroke The `KeyStroke` that wasn't handled by the GUI
 * @return `true` if at least one of the listeners handled the key stroke, this will signal to the GUI that it
 * needs to be redrawn again.
 */
    protected fun fireUnhandledKeyStroke(keyStroke:KeyStroke?):Boolean {
var handled = false
for (listener in listeners!!)
{
handled = listener!!.onUnhandledKeyStroke(this, keyStroke) || handled
}
return handled
}

/**
 * Marks the whole text GUI as invalid and that it needs to be redrawn at next opportunity
 */
    protected fun invalidate() {
dirty = true
}

/**
 * Draws the entire GUI using a `TextGUIGraphics` object
 * @param graphics Graphics object to draw using
 */
    protected abstract fun drawGUI(graphics:TextGUIGraphics?) 

/**
 * This method should take the user input and feed it to the focused component for handling.
 * @param key `KeyStroke` representing the user input
 * @return `true` if the input was recognized and handled by the GUI, indicating that the GUI should be redrawn
 */
    protected abstract fun handleInput(key:KeyStroke?):Boolean 
}
