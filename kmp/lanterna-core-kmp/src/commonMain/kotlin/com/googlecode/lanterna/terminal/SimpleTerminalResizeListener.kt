/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal

import com.googlecode.lanterna.TerminalSize

/**
 * This class is a simple implementation of Terminal.ResizeListener which will keep track of the size of the terminal
 * and let you know if the terminal has been resized since you last checked. This can be useful to avoid threading
 * problems with the resize callback when your application is using a main event loop.
 *
 * @author martin
 */
@Suppress("WeakerAccess")
open class SimpleTerminalResizeListener(initialSize: TerminalSize?) : TerminalResizeListener {

    internal var wasResized: Boolean = false
    internal var lastKnownSize: TerminalSize? = initialSize

    /**
     * Checks if the terminal was resized since the last time this method was called. If this is the first time calling
     * this method, the result is going to be based on if the terminal has been resized since this listener was attached
     * to the Terminal.
     *
     * @return true if the terminal was resized, false otherwise
     */
    @Synchronized
    open fun isTerminalResized(): Boolean {
        return if (wasResized) {
            wasResized = false
            true
        } else {
            false
        }
    }

    /**
     * Returns the last known size the Terminal is supposed to have.
     *
     * @return Size of the terminal, as of the last resize update
     */
    open fun getLastKnownSize(): TerminalSize? {
        return lastKnownSize
    }

    @Synchronized
    override open fun onResized(terminal: Terminal?, newSize: TerminalSize?) {
        this.wasResized = true
        this.lastKnownSize = newSize
    }
}
