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

import com.googlecode.lanterna.bundle.LocalizedUIBundle
import java.util.Locale

/**
 * Set of predefined localized string.<br>
 * All this strings are localized by using [LocalizedUIBundle].<br>
 * Changing the locale by calling [Locale.setDefault].
 * @author silveryocha.
 */
class LocalizedString private constructor(
    private val bundleKey: String,
    private val defaultValue: String,
) {
    override fun toString(): String {
        return LocalizedUIBundle.get(Locale.getDefault(), bundleKey) ?: defaultValue
    }

    companion object {
        /**
         * "OK"
         */
        val OK = LocalizedString("short.label.ok", "OK")

        /**
         * "Cancel"
         */
        val Cancel = LocalizedString("short.label.cancel", "Cancel")

        /**
         * "Yes"
         */
        val Yes = LocalizedString("short.label.yes", "Yes")

        /**
         * "No"
         */
        val No = LocalizedString("short.label.no", "No")

        /**
         * "Close"
         */
        val Close = LocalizedString("short.label.close", "Close")

        /**
         * "Abort"
         */
        val Abort = LocalizedString("short.label.abort", "Abort")

        /**
         * "Ignore"
         */
        val Ignore = LocalizedString("short.label.ignore", "Ignore")

        /**
         * "Retry"
         */
        val Retry = LocalizedString("short.label.retry", "Retry")

        /**
         * "Continue"
         */
        val Continue = LocalizedString("short.label.continue", "Continue")

        /**
         * "Open"
         */
        val Open = LocalizedString("short.label.open", "Open")

        /**
         * "Save"
         */
        val Save = LocalizedString("short.label.save", "Save")
    }
}
