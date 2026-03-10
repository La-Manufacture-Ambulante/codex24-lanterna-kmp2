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
import java.util.Collections
import java.util.HashSet

/**
 * Abstract class for dialog building, containing much shared code between different kinds of dialogs.
 */
abstract class AbstractDialogBuilder<B, T : DialogWindow>(initialTitle: String?) {
    private var dialogTitle: String? = initialTitle
    private var dialogDescription: String? = null
    private var dialogExtraWindowHints: Set<Window.Hint?> = Collections.singleton(Window.Hint.CENTERED)

    /**
     * Changes the title of the dialog.
     */
    fun setTitle(title: String?): B {
        this.dialogTitle = title ?: ""
        return self()
    }

    /**
     * Returns the title that the built dialog will have.
     */
    fun getTitle(): String? = dialogTitle

    /**
     * Changes the description of the dialog.
     */
    fun setDescription(description: String?): B {
        this.dialogDescription = description
        return self()
    }

    /**
     * Returns the description that the built dialog will have.
     */
    fun getDescription(): String? = dialogDescription

    /**
     * Assigns extra window hints that should be applied to the built dialog.
     */
    fun setExtraWindowHints(extraWindowHints: Set<Window.Hint?>?): B {
        this.dialogExtraWindowHints = extraWindowHints ?: emptySet()
        return self()
    }

    /**
     * Returns extra window hints that will be assigned when built.
     */
    fun getExtraWindowHints(): Set<Window.Hint?> = dialogExtraWindowHints

    protected abstract fun self(): B

    protected abstract fun buildDialog(): T

    /**
     * Builds a new dialog following the specifications of this builder.
     */
    fun build(): T {
        val dialog = buildDialog()
        if (dialogExtraWindowHints.isNotEmpty()) {
            val combinedHints = HashSet(dialog.hints.orEmpty())
            combinedHints.addAll(dialogExtraWindowHints)
            dialog.setHints(combinedHints)
        }
        return dialog
    }
}
