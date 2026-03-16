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
import com.googlecode.lanterna.internal.compat.Properties

/**
 * [Theme] implementation that stores the theme definition in a regular java Properties object. The format is:
 * <pre>foreground = black
 * background = white
 * sgr =
 * com.mypackage.mycomponent.MyClass.foreground = yellow
 * com.mypackage.mycomponent.MyClass.background = white
 * com.mypackage.mycomponent.MyClass.sgr =
 * com.mypackage.mycomponent.MyClass.foreground[ACTIVE] = red
 * com.mypackage.mycomponent.MyClass.background[ACTIVE] = black
 * com.mypackage.mycomponent.MyClass.sgr[ACTIVE] = bold
 * ...</pre>
 *
 * See the documentation on [Theme] for further information about different style categories that can be assigned.
 * The foreground, background and sgr entries without a class specifier will be tied to the global fallback and is used
 * if the libraries tries to apply a theme style that isn't specified in the Properties object and there is no other
 * superclass specified either.
 */
open class PropertyTheme(
    properties: Properties,
    ignoreUnknownClasses: Boolean = false,
) : AbstractTheme(
        instanceByClassName(properties.getProperty("postrenderer", "")) as? WindowPostRenderer,
        instanceByClassName(properties.getProperty("windowdecoration", "")) as? WindowDecorationRenderer,
    ) {
    init {
        for (key in properties.stringPropertyNames()) {
            val definition = getDefinition(key!!)
            if (!addStyle(definition, getStyle(key!!), properties.getProperty(key))) {
                if (!ignoreUnknownClasses) {
                    throw IllegalArgumentException("Unknown class encountered when parsing theme: '$definition'")
                }
            }
        }
    }

    constructor(properties: Properties) : this(properties, false)

    private fun getDefinition(propertyName: String): String {
        return if (!propertyName.contains(".")) {
            ""
        } else {
            propertyName.substring(0, propertyName.lastIndexOf("."))
        }
    }

    private fun getStyle(propertyName: String): String {
        return if (!propertyName.contains(".")) {
            propertyName
        } else {
            propertyName.substring(propertyName.lastIndexOf(".") + 1)
        }
    }
}
