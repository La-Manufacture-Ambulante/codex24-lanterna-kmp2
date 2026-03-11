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
import kotlin.reflect.KClass

/**
 * Allows you to more easily wrap an existing theme definition and alter the behaviour in some special cases. You
 * normally create a new class that extends from this and override some of the methods to divert the call depending on
 * what you are trying to do. For an example, please see Issue409 in the test code.
 *
 * Creates a new [DelegatingThemeDefinition] with a default implementation that forwards all calls to
 * [themeDefinition].
 * @param themeDefinition Other theme definition to delegate all calls to
 *
 * @see DelegatingTheme
 * @see DefaultMutableThemeStyle
 * @see Theme
 */
open class DelegatingThemeDefinition(private val themeDefinition: ThemeDefinition) : ThemeDefinition {
    override val normal: ThemeStyle?
        get() = themeDefinition.normal

    override val preLight: ThemeStyle?
        get() = themeDefinition.preLight

    override val selected: ThemeStyle?
        get() = themeDefinition.selected

    override val active: ThemeStyle?
        get() = themeDefinition.active

    override val insensitive: ThemeStyle?
        get() = themeDefinition.insensitive

    override fun getCustom(name: String?): ThemeStyle? {
        return themeDefinition.getCustom(name)
    }

    override fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle? {
        return themeDefinition.getCustom(name, defaultValue)
    }

    override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
        return themeDefinition.getBooleanProperty(name, defaultValue)
    }

    override fun getIntegerProperty(name: String?, defaultValue: Int): Int {
        return themeDefinition.getIntegerProperty(name, defaultValue)
    }

    override val isCursorVisible: Boolean
        get() = themeDefinition.isCursorVisible

    override fun getCharacter(name: String?, fallback: Char): Char {
        return themeDefinition.getCharacter(name, fallback)
    }

    override fun <T : Component> getRenderer(type: KClass<T>?): ComponentRenderer<T?>? {
        return themeDefinition.getRenderer(type)
    }
}
