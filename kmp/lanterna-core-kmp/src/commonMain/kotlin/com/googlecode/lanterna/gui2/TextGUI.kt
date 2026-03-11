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

import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.internal.io.IOException

/**
 * Base interface for advanced text GUIs supported in Lanterna.
 */
interface TextGUI {
    var theme: Theme?

    val screen: Screen?

    val isPendingUpdate: Boolean

    val guiThread: TextGUIThread?

    val focusedInteractable: Interactable?

    @Throws(IOException::class)
    fun processInput(): Boolean

    @Throws(IOException::class)
    fun updateScreen()

    fun setVirtualScreenEnabled(virtualScreenEnabled: Boolean)

    fun addListener(listener: Listener?)

    fun removeListener(listener: Listener?)

    interface Listener {
        fun onUnhandledKeyStroke(textGUI: TextGUI?, keyStroke: KeyStroke?): Boolean
    }
}
