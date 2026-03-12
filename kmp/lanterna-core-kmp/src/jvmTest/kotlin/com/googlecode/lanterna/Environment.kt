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
 * Copyright (C) 2010-2024 Martin Berglund
 */

package com.googlecode.lanterna

import java.util.ArrayList
import java.util.Collections
import java.util.Properties

/**
 *
 * @author martin
 */
object Environment {
    fun main(args: Array<String?>?) {
        val properties: Properties = System.getProperties()
        val propertyKeys = ArrayList(properties.stringPropertyNames())
        Collections.sort(propertyKeys)
        for (key in propertyKeys) {
            println("$key = ${properties.getProperty(key)}")
        }

        val envKeys = ArrayList(System.getenv().keys)
        Collections.sort(envKeys)
        for (key in envKeys) {
            println("$key = ${System.getenv(key)}")
        }
    }
}
