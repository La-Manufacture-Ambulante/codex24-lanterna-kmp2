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
 * Available selection of buttons that can be added to a [MessageDialog].
 */
enum class MessageDialogButton(private val label: LocalizedString) {
    OK(LocalizedString.OK),
    CANCEL(LocalizedString.Cancel),
    YES(LocalizedString.Yes),
    NO(LocalizedString.No),
    CLOSE(LocalizedString.Close),
    ABORT(LocalizedString.Abort),
    IGNORE(LocalizedString.Ignore),
    RETRY(LocalizedString.Retry),
    CONTINUE(LocalizedString.Continue),
    ;

    /**
     * Returns the localized button label.
     */
    override fun toString(): String {
        return label.toString()
    }
}
