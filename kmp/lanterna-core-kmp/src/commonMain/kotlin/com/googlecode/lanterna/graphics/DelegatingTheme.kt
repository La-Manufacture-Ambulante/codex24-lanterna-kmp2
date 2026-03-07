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

import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer

/**
 * Allows you to more easily wrap an existing theme and alter the behaviour in some special cases. You normally create a
 * new class that extends from this and override some of the methods to divert the call depending on what you are trying
 * to do. For an example, please see Issue409 in the test code.
 * @see DelegatingThemeDefinition
 * 
 * @see DefaultMutableThemeStyle
 * 
 * @see Theme
 */
 class DelegatingTheme/**
 * Creates a new [DelegatingTheme] with a default implementation that will forward all calls to the
 * [Theme] that is passed in.
 * @param theme Other theme to delegate all calls to
 */
    (private val theme:Theme?):Theme {

 val defaultDefinition:ThemeDefinition?
@Override
get() {
return theme!!.getDefaultDefinition()
}

 val windowPostRenderer:WindowPostRenderer?
@Override
get() {
return theme!!.getWindowPostRenderer()
}

 val windowDecorationRenderer:WindowDecorationRenderer?
@Override
get() {
return theme!!.getWindowDecorationRenderer()
}

@Override
 fun getDefinition(clazz:Class<*>?):ThemeDefinition? {
return theme!!.getDefinition(clazz)
}
}
