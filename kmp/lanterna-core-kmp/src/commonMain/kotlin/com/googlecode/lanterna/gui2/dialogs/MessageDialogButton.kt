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

import com.googlecode.lanterna.gui2.LocalizedString

/**
 * Available selection of buttons that can be added to a [MessageDialog]. These values are also used as the
 * result when a dialog button is selected.
 */
enum class MessageDialogButton(private val label: LocalizedString) {
    /** "OK" button. */
    OK(LocalizedString.OK),
    /** "Cancel" button. */
    CANCEL(LocalizedString.Cancel),
    /** "Yes" button. */
    YES(LocalizedString.Yes),
    /** "No" button. */
    NO(LocalizedString.No),
    /** "Close" button. */
    CLOSE(LocalizedString.Close),
    /** "Abort" button. */
    ABORT(LocalizedString.Abort),
    /** "Ignore" button. */
    IGNORE(LocalizedString.Ignore),
    /** "Retry" button. */
    RETRY(LocalizedString.Retry),
    /** "Continue" button. */
    CONTINUE(LocalizedString.Continue),
    ;

    /**
     * Returns the localized button label.
     */
    override fun toString(): String {
        return label.toString()
    }
}
