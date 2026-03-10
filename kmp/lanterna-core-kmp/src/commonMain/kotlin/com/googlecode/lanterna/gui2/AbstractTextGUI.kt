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
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Abstract implementation of [TextGUI] with shared screen/input logic.
 */
abstract class AbstractTextGUI protected constructor(textGUIThreadFactory: TextGUIThreadFactory, override val screen: Screen?) : TextGUI {
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

    @Throws(IOException::class)
    protected open fun readKeyStroke(): KeyStroke? {
        val activeScreen = screen ?: return null
        return if (blockingIO) activeScreen.readInput() else pollInput()
    }

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

    fun isBlockingIO(): Boolean {
        return blockingIO
    }

    protected fun fireUnhandledKeyStroke(keyStroke: KeyStroke?): Boolean {
        var handled = false
        for (listener in listeners) {
            handled = listener.onUnhandledKeyStroke(this, keyStroke) || handled
        }
        return handled
    }

    protected fun invalidate() {
        dirty = true
    }

    protected abstract fun drawGUI(graphics: TextGUIGraphics?)

    protected abstract val cursorPosition: TerminalPosition?

    protected abstract fun handleInput(key: KeyStroke?): Boolean
}
