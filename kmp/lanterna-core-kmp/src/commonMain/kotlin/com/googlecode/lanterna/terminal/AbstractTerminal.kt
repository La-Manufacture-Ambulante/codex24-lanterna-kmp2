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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.internal.io.IOException

abstract class AbstractTerminal protected constructor() : Terminal {
    private val resizeListeners: MutableList<TerminalResizeListener> = ArrayList()
    private var lastKnownSize: TerminalSize? = null

    override fun addResizeListener(listener: TerminalResizeListener?) {
        if (listener != null) {
            resizeListeners.add(listener)
        }
    }

    override fun removeResizeListener(listener: TerminalResizeListener?) {
        if (listener != null) {
            resizeListeners.remove(listener)
        }
    }

    protected fun onResized(columns: Int, rows: Int) {
        onResized(TerminalSize(columns, rows))
    }

    protected fun onResized(newSize: TerminalSize?) {
        if (lastKnownSize == null || lastKnownSize != newSize) {
            lastKnownSize = newSize
            for (resizeListener in resizeListeners) {
                resizeListener.onResized(this, lastKnownSize)
            }
        }
    }

    @Throws(IOException::class)
    override fun newTextGraphics(): TextGraphics {
        return TerminalTextGraphics(this)
    }
}
