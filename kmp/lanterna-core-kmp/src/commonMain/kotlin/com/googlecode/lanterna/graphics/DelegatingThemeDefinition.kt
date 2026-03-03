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
package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer

/**
 * Allows you to more easily wrap an existing theme definion and alter the behaviour in some special cases. You normally
 * create a new class that extends from this and override some of the methods to divert the call depending on what you
 * are trying to do. For an example, please see Issue409 in the test code.
 * @see DelegatingTheme
 * @see DefaultMutableThemeStyle
 * @see Theme
 */
open class DelegatingThemeDefinition(themeDefinition: ThemeDefinition?) : ThemeDefinition {
    private val themeDefinition: ThemeDefinition? = themeDefinition

    private fun delegate(): ThemeDefinition {
        val value = themeDefinition
        if (value == null) {
            throw NullPointerException()
        }
        return value
    }

    override open fun getNormal(): ThemeStyle? {
        return delegate().getNormal()
    }

    override open fun getPreLight(): ThemeStyle? {
        return delegate().getPreLight()
    }

    override open fun getSelected(): ThemeStyle? {
        return delegate().getSelected()
    }

    override open fun getActive(): ThemeStyle? {
        return delegate().getActive()
    }

    override open fun getInsensitive(): ThemeStyle? {
        return delegate().getInsensitive()
    }

    override open fun getCustom(name: String?): ThemeStyle? {
        return delegate().getCustom(name)
    }

    override open fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle? {
        return delegate().getCustom(name, defaultValue)
    }

    override open fun getIntegerProperty(name: String?, defaultValue: Int): Int {
        return delegate().getIntegerProperty(name, defaultValue)
    }

    override open fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
        return delegate().getBooleanProperty(name, defaultValue)
    }

    override open fun isCursorVisible(): Boolean {
        return delegate().isCursorVisible()
    }

    override open fun getCharacter(name: String?, fallback: Char): Char {
        return delegate().getCharacter(name, fallback)
    }

    override open fun <T : Component?> getRenderer(type: Class<T>?): ComponentRenderer<T>? {
        return delegate().getRenderer(type)
    }
}
