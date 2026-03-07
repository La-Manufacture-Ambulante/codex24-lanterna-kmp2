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
 * 
 * @see DefaultMutableThemeStyle
 * 
 * @see Theme
 */
 class DelegatingThemeDefinition/**
 * Creates a new [DelegatingThemeDefinition] with a default implementation that will forward all calls to the
 * [ThemeDefinition] that is passed in.
 * @param themeDefinition Other theme definition to delegate all calls to
 */
    (private val themeDefinition:ThemeDefinition?):ThemeDefinition {

 val normal:ThemeStyle?
@Override
get() {
return themeDefinition!!.getNormal()
}

 val preLight:ThemeStyle?
@Override
get() {
return themeDefinition!!.getPreLight()
}

 val selected:ThemeStyle?
@Override
get() {
return themeDefinition!!.getSelected()
}

 val active:ThemeStyle?
@Override
get() {
return themeDefinition!!.getActive()
}

 val insensitive:ThemeStyle?
@Override
get() {
return themeDefinition!!.getInsensitive()
}

 val isCursorVisible:Boolean
@Override
get() {
return themeDefinition!!.isCursorVisible()
}

@Override
 fun getCustom(name:String?):ThemeStyle? {
return themeDefinition!!.getCustom(name)
}

@Override
 fun getCustom(name:String?, defaultValue:ThemeStyle?):ThemeStyle? {
return themeDefinition!!.getCustom(name, defaultValue)
}

@Override
 fun getIntegerProperty(name:String?, defaultValue:Int):Int {
return themeDefinition!!.getIntegerProperty(name, defaultValue)
}

@Override
 fun getBooleanProperty(name:String?, defaultValue:Boolean):Boolean {
return themeDefinition!!.getBooleanProperty(name, defaultValue)
}

@Override
 fun getCharacter(name:String?, fallback:Char):Char {
return themeDefinition!!.getCharacter(name, fallback)
}

@Override
 fun <T : Component?> getRenderer(type:Class<T?>?):ComponentRenderer<T?>? {
return themeDefinition!!.getRenderer(type)
}
}
