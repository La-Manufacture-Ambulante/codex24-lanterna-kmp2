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
package com.googlecode.lanterna.gui2.dialogs

import com.googlecode.lanterna.gui2.AbstractWindow
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import java.util.Collections
import java.util.HashSet

/**
 * Thin layer on top of [AbstractWindow] that automatically sets properties and hints to make it act as a modal dialog
 * window.
 */
abstract class DialogWindow protected constructor(title: String?) : AbstractWindow(title) {
    init {
        setHints(GLOBAL_DIALOG_HINTS)
    }

    /**
     * Opens the dialog by showing it on the GUI and does not return until the dialog has been closed.
     * @param textGUI Text GUI to add the dialog to
     * @return Depending on the [DialogWindow] implementation, by default `null`
     */
    open fun showDialog(textGUI: WindowBasedTextGUI): Any? {
        textGUI.addWindow(this)
        waitUntilClosed()
        return null
    }

    companion object {
        private val GLOBAL_DIALOG_HINTS: Set<Window.Hint?> =
            Collections.unmodifiableSet(HashSet(Collections.singletonList(Window.Hint.MODAL)))
    }
}
