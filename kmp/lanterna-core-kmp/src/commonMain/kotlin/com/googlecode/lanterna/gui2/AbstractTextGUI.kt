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

abstract class AbstractTextGUI protected constructor(textGUIThreadFactory: TextGUIThreadFactory?, screen: Screen?) : TextGUI {
    private val screen: Screen
    private val listeners: MutableList<Listener?>
    private var blockingIO: Boolean
    private var dirty: Boolean
    private var textGUIThread: TextGUIThread?
    private var guiTheme: Theme?

    init {
        if (screen == null) {
            throw IllegalArgumentException("Creating a TextGUI requires an underlying Screen")
        }
        this.screen = screen
        this.listeners = CopyOnWriteArrayList()
        this.blockingIO = false
        this.dirty = false
        this.guiTheme = LanternaThemes.getDefaultTheme()
        this.textGUIThread = (textGUIThreadFactory ?: throw NullPointerException()).createTextGUIThread(this)
    }

    @Throws(IOException::class)
    protected open fun readKeyStroke(): KeyStroke? {
        return if (blockingIO) screen.readInput() else pollInput()
    }

    @Throws(IOException::class)
    protected open fun pollInput(): KeyStroke? {
        return screen.pollInput()
    }

    @Synchronized
    @Throws(IOException::class)
    override fun processInput(): Boolean {
        var gotInput = false
        var keyStroke = readKeyStroke()
        if (keyStroke != null) {
            gotInput = true
            do {
                val currentKeyStroke = keyStroke ?: throw NullPointerException()
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

    override fun setTheme(theme: Theme?) {
        if (theme != null) {
            this.guiTheme = theme
        }
    }

    override fun getTheme(): Theme? {
        return guiTheme
    }

    @Synchronized
    @Throws(IOException::class)
    override fun updateScreen() {
        screen.doResizeIfNecessary()
        drawGUI(DefaultTextGUIGraphics(this, screen.newTextGraphics()))
        screen.setCursorPosition(getCursorPosition())
        screen.refresh()
        dirty = false
    }

    override fun getScreen(): Screen {
        return screen
    }

    override fun isPendingUpdate(): Boolean {
        return screen.doResizeIfNecessary() != null || dirty
    }

    override fun getGUIThread(): TextGUIThread? {
        return textGUIThread
    }

    override fun addListener(listener: Listener?) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener?) {
        listeners.remove(listener)
    }

    open fun setBlockingIO(blockingIO: Boolean) {
        this.blockingIO = blockingIO
    }

    open fun isBlockingIO(): Boolean {
        return blockingIO
    }

    protected fun fireUnhandledKeyStroke(keyStroke: KeyStroke?): Boolean {
        var handled = false
        for (listener in listeners) {
            val nonNullListener = listener ?: throw NullPointerException()
            handled = nonNullListener.onUnhandledKeyStroke(this, keyStroke) || handled
        }
        return handled
    }

    protected open fun invalidate() {
        dirty = true
    }

    protected abstract fun drawGUI(graphics: TextGUIGraphics)

    protected abstract fun getCursorPosition(): TerminalPosition?

    protected abstract fun handleInput(key: KeyStroke): Boolean
}
