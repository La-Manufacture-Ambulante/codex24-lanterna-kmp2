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
import com.googlecode.lanterna.internal.io.EOFException
import com.googlecode.lanterna.internal.io.IOException
import com.googlecode.lanterna.internal.compat.CopyOnWriteArrayList

/**
 * This abstract implementation of TextGUI contains some basic management of the underlying Screen and other common code
 * that can be shared between different implementations.
 * @author Martin
 */
abstract class AbstractTextGUI
/**
 * Constructor for `AbstractTextGUI` that requires a `Screen` and a factory for creating the GUI thread
 * @param textGUIThreadFactory Factory class to use for creating the `TextGUIThread` class
 * @param screen What underlying `Screen` to use for this text GUI
 */
protected constructor(textGUIThreadFactory: TextGUIThreadFactory, override val screen: Screen?) : TextGUI {
    private val listeners: MutableList<TextGUI.Listener> = CopyOnWriteArrayList()
    private var blockingIO: Boolean = false
    private var dirty: Boolean = false
    private var textGUIThread: TextGUIThread? = null
    private var guiTheme: Theme? = null

    init {
        if (screen == null) {
            throw IllegalArgumentException("Creating a TextGUI requires an underlying Screen")
        }
        guiTheme = LanternaThemes.defaultTheme
        textGUIThread = textGUIThreadFactory.createTextGUIThread(this)
    }

    /**
     * Reads one key from the input queue, blocking or non-blocking depending on if blocking I/O has been enabled. To
     * enable blocking I/O (disabled by default), use `setBlockingIO(true)`.
     * @return One piece of user input as a `KeyStroke` or `null` if blocking I/O is disabled and there was
     * no input waiting
     * @throws IOException In case of an I/O error while reading input
     */
    @Throws(IOException::class)
    protected open fun readKeyStroke(): KeyStroke? {
        val activeScreen = screen ?: return null
        return if (blockingIO) activeScreen.readInput() else pollInput()
    }

    /**
     * Polls the underlying input queue for user input, returning either a `KeyStroke` or `null`
     * @return `KeyStroke` representing the user input or `null` if there was none
     * @throws IOException In case of an I/O error while reading input
     */
    @Throws(IOException::class)
    protected fun pollInput(): KeyStroke? {
        return screen?.pollInput()
    }

    @Throws(IOException::class)
    override fun processInput(): Boolean {
        var gotInput = false
        var keyStroke = readKeyStroke()
        if (keyStroke != null) {
            gotInput = true
            do {
                val currentKeyStroke = keyStroke ?: break
                if (currentKeyStroke.keyType == KeyType.EOF) {
                    throw EOFException()
                }
                var handled = handleInput(currentKeyStroke)
                if (!handled) {
                    handled = fireUnhandledKeyStroke(currentKeyStroke)
                }
                dirty = handled || dirty
                keyStroke = pollInput()
            } while (keyStroke != null)
        }
        return gotInput
    }

    override var theme: Theme?
        get() = guiTheme
        set(value) {
            if (value != null) {
                guiTheme = value
            }
        }

    @Throws(IOException::class)
    override open fun updateScreen() {
        val activeScreen = screen ?: return
        activeScreen.doResizeIfNecessary()
        drawGUI(DefaultTextGUIGraphics(this, activeScreen.newTextGraphics()))
        activeScreen.cursorPosition = cursorPosition
        activeScreen.refresh()
        dirty = false
    }

    override open val isPendingUpdate: Boolean
        get() {
            val resized = screen?.doResizeIfNecessary() != null
            return resized || dirty
        }

    override val guiThread: TextGUIThread?
        get() = textGUIThread

    override fun addListener(listener: TextGUI.Listener?) {
        if (listener != null) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: TextGUI.Listener?) {
        if (listener != null) {
            listeners.remove(listener)
        }
    }

    fun setBlockingIO(blockingIO: Boolean) {
        this.blockingIO = blockingIO
    }

    /**
     * Checks if blocking I/O is enabled or not
     * @return `true` if blocking I/O is enabled, otherwise `false`
     */
    fun isBlockingIO(): Boolean {
        return blockingIO
    }

    /**
     * This method should be called when there was user input that wasn't handled by the GUI. It will fire the
     * `onUnhandledKeyStroke(..)` method on any registered listener.
     * @param keyStroke The `KeyStroke` that wasn't handled by the GUI
     * @return `true` if at least one of the listeners handled the key stroke, this will signal to the GUI that it
     * needs to be redrawn again.
     */
    protected fun fireUnhandledKeyStroke(keyStroke: KeyStroke?): Boolean {
        var handled = false
        for (listener in listeners) {
            handled = listener.onUnhandledKeyStroke(this, keyStroke) || handled
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
    protected abstract fun drawGUI(graphics: TextGUIGraphics?)

    /**
     * Top-level method for drilling in to the GUI and figuring out, in global coordinates, where to place the text
     * cursor on the screen at this time.
     * @return Where to place the text cursor, or `null` if the cursor should be hidden
     */
    protected abstract val cursorPosition: TerminalPosition?

    /**
     * This method should take the user input and feed it to the focused component for handling.
     * @param key `KeyStroke` representing the user input
     * @return `true` if the input was recognized and handled by the GUI, indicating that the GUI should be redrawn
     */
    protected abstract fun handleInput(key: KeyStroke?): Boolean
}
