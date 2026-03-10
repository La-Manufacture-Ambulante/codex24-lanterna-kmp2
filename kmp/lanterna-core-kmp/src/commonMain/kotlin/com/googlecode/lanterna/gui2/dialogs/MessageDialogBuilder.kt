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

import com.googlecode.lanterna.gui2.Window
import java.util.ArrayList
import java.util.HashSet

/**
 * Dialog builder for the [MessageDialog] class.
 */
class MessageDialogBuilder {
    private var title: String = "MessageDialog"
    private var text: String = "Text"
    private val buttons: MutableList<MessageDialogButton> = ArrayList()
    private val extraWindowHints: MutableSet<Window.Hint?> = HashSet()

    init {
        extraWindowHints.add(Window.Hint.CENTERED)
        extraWindowHints.add(Window.Hint.MODAL)
    }

    fun build(): MessageDialog {
        val messageDialog = MessageDialog(title, text, *buttons.toTypedArray())
        messageDialog.setHints(extraWindowHints)
        return messageDialog
    }

    fun setTitle(title: String?): MessageDialogBuilder {
        this.title = title ?: ""
        return this
    }

    /**
     * Sets message text.
     */
    fun setText(text: String?): MessageDialogBuilder {
        this.text = text ?: ""
        return this
    }

    /**
     * Replaces the extra window hints used when building the dialog.
     */
    fun setExtraWindowHints(extraWindowHints: Collection<Window.Hint?>?): MessageDialogBuilder {
        this.extraWindowHints.clear()
        this.extraWindowHints.addAll(extraWindowHints.orEmpty())
        return this
    }

    /**
     * Adds a message dialog button.
     */
    fun addButton(button: MessageDialogButton?): MessageDialogBuilder {
        if (button != null) {
            buttons.add(button)
        }
        return this
    }
}
