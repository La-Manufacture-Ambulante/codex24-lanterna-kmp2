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

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.internal.compat.EnumSet

/**
 * This basic implementation of ThemeStyle keeps the styles in its internal state and allows you to mutate them. It can
 * be used to more easily override an existing theme and make small changes programmatically to it, see Issue409 in the
 * test section for an example of how to do this.
 * @see DelegatingThemeDefinition
 *
 * @see DelegatingTheme
 *
 * @see Theme
 */
class DefaultMutableThemeStyle : ThemeStyle {
    override var foreground: TextColor? = null
    override var background: TextColor? = null
    private var sgrs: EnumSet<SGR>? = null

    override val sgRs: EnumSet<SGR>?
        get() {
            val current = sgrs
            return if (current == null) EnumSet.noneOf(SGR::class) else EnumSet.copyOf(current)
        }

/**
     * Creates a new [DefaultMutableThemeStyle] based on an existing [ThemeStyle]. The values of this style
     * that is passed in will be copied into the new object that is created.
     * @param themeStyleToCopy [ThemeStyle] object to copy the style parameters from
     */
    constructor(themeStyleToCopy: ThemeStyle) : this(
        themeStyleToCopy.foreground,
        themeStyleToCopy.background,
        themeStyleToCopy.sgRs,
    ) {}

/**
     * Creates a new [DefaultMutableThemeStyle] with a specified style (foreground, background and SGR state)
     * @param foreground Foreground color of the text with this style
     * @param background Background color of the text with this style
     * @param sgrs Modifiers to apply to the text with this style
     */
    constructor(
        foreground: TextColor?,
        background: TextColor?,
        vararg sgrs: SGR?,
    ) : this(foreground, background, if (sgrs.size > 0) EnumSet.copyOf(listOf(*sgrs).filterNotNull()) else EnumSet.noneOf(SGR::class)) {}

    private constructor(foreground: TextColor?, background: TextColor?, sgrs: EnumSet<SGR>?) {
        if (foreground == null) {
            throw IllegalArgumentException("Cannot set SimpleTheme's style foreground to null")
        }
        if (background == null) {
            throw IllegalArgumentException("Cannot set SimpleTheme's style background to null")
        }
        this.foreground = foreground
        this.background = background
        this.sgrs = if (sgrs == null) EnumSet.noneOf(SGR::class) else EnumSet.copyOf(sgrs)
    }

/**
     * Modifies the foreground color of this [DefaultMutableThemeStyle] to the value passed in
     * @param foreground New foreground color for this theme style
     * @return Itself
     */
    fun setForeground(foreground: TextColor?): DefaultMutableThemeStyle {
        this.foreground = foreground
        return this
    }

/**
     * Modifies the background color of this [DefaultMutableThemeStyle] to the value passed in
     * @param background New background color for this theme style
     * @return Itself
     */
    fun setBackground(background: TextColor?): DefaultMutableThemeStyle {
        this.background = background
        return this
    }

/**
     * Modifies the SGR modifiers of this [DefaultMutableThemeStyle] to the values passed it.
     * @param sgrs New SGR modifiers for this theme style, the values in this set will be copied into the internal state
     * @return Itself
     */
    fun setSGRs(sgrs: EnumSet<SGR>?): DefaultMutableThemeStyle {
        this.sgrs = if (sgrs == null) EnumSet.noneOf(SGR::class) else EnumSet.copyOf(sgrs)
        return this
    }
}
