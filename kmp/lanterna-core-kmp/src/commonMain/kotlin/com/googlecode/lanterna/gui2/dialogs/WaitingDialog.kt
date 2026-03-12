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

import com.googlecode.lanterna.gui2.AnimatedLabel
import com.googlecode.lanterna.gui2.Label
import com.googlecode.lanterna.gui2.Panels
import com.googlecode.lanterna.gui2.WindowBasedTextGUI

/**
 * Dialog that displays a text message and optional spinning indicator.
 */
class WaitingDialog private constructor(title: String?, text: String?) : DialogWindow(title) {
    init {
        val mainPanel = Panels.horizontal(Label(text), AnimatedLabel.createClassicSpinningLine())
        component = mainPanel
    }

    override fun showDialog(textGUI: WindowBasedTextGUI): Any? {
        showDialog(textGUI, true)
        return null
    }

    fun showDialog(
        textGUI: WindowBasedTextGUI,
        blockUntilClosed: Boolean,
    ) {
        textGUI.addWindow(this)
        if (blockUntilClosed) {
            waitUntilClosed()
        }
    }

    companion object {
        fun createDialog(
            title: String?,
            text: String?,
        ): WaitingDialog {
            return WaitingDialog(title, text)
        }

        fun showDialog(
            textGUI: WindowBasedTextGUI,
            title: String?,
            text: String?,
        ): WaitingDialog {
            val waitingDialog = createDialog(title, text)
            waitingDialog.showDialog(textGUI, false)
            return waitingDialog
        }
    }
}
