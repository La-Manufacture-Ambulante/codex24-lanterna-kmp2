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
package com.googlecode.lanterna.gui2

import java.util.Collections
import java.util.LinkedList

/**
 *
 * @author ginkoblongata
 */
class WindowList {
    private val windows = LinkedList<Window>()
    private val stableOrderingOfWindows = mutableListOf<Window>()

    var activeWindow: Window? = null
        set(activeWindow) {
            field = activeWindow
            if (activeWindow != null) {
                moveToTop(activeWindow)
            }
        }

    var isHadWindowAtSomePoint: Boolean = false
        private set

    val windowsInZOrder: List<Window>
        get() = Collections.unmodifiableList(windows)

    val windowsInStableOrder: List<Window>
        get() = Collections.unmodifiableList(stableOrderingOfWindows)

    fun addWindow(window: Window?) {
        if (!stableOrderingOfWindows.contains(window)) {
            stableOrderingOfWindows.add(window!!)
        }
        if (!windows.contains(window)) {
            windows.add(window!!)
        }
        if (!window!!.hints!!.contains(Window.Hint.NO_FOCUS)) {
            activeWindow = window
        }
        isHadWindowAtSomePoint = true
    }

    fun removeWindow(window: Window?): Boolean {
        val contained = windows.remove(window)
        stableOrderingOfWindows.remove(window)

        if (activeWindow === window) {
            activeWindow = null
            for (index in windows.size - 1 downTo 0) {
                val candidate = windows[index]
                if (candidate.hints?.contains(Window.Hint.NO_FOCUS) != true) {
                    activeWindow = candidate
                    break
                }
            }
        }

        return contained
    }

    fun moveToTop(window: Window?) {
        require(windows.contains(window)) { "Window $window isn't in MultiWindowTextGUI $this" }
        windows.remove(window)
        windows.add(window!!)
    }

    fun moveToBottom(window: Window?) {
        require(windows.contains(window)) { "Window $window isn't in MultiWindowTextGUI $this" }
        windows.remove(window)
        windows.add(0, window!!)
    }

    fun cycleActiveWindow(reverse: Boolean): WindowList {
        if (windows.isEmpty() || windows.size == 1 || activeWindow?.hints?.contains(Window.Hint.MODAL) == true) {
            return this
        }

        val originalActiveWindow = activeWindow
        var nextWindow =
            if (originalActiveWindow == null) {
                if (reverse) windows.last else windows.first
            } else {
                getNextWindow(reverse, originalActiveWindow)
            }

        var noFocusWindows = 0
        while (nextWindow.hints?.contains(Window.Hint.NO_FOCUS) == true) {
            noFocusWindows++
            if (noFocusWindows == windows.size) {
                return this
            }
            nextWindow = getNextWindow(reverse, nextWindow)
            if (nextWindow === originalActiveWindow) {
                return this
            }
        }

        if (reverse) {
            moveToTop(nextWindow)
        } else if (originalActiveWindow != null) {
            moveToBottom(originalActiveWindow)
        }
        activeWindow = nextWindow
        return this
    }

    private fun getNextWindow(
        reverse: Boolean,
        window: Window,
    ): Window {
        var index = windows.indexOf(window)
        if (reverse) {
            index++
            if (index >= windows.size) {
                index = 0
            }
        } else {
            index--
            if (index < 0) {
                index = windows.size - 1
            }
        }
        return windows[index]
    }
}
