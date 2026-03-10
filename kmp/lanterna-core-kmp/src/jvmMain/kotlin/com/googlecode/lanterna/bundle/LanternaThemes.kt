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
package com.googlecode.lanterna.bundle

import com.googlecode.lanterna.graphics.PropertyTheme
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.gui2.AbstractTextGUI
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

/**
 * Catalog of available themes, this class will initially contain the themes bundled with Lanterna but it is possible to
 * add additional themes as well.
 */
object LanternaThemes {
    private val REGISTERED_THEMES = ConcurrentHashMap<String, Theme>()

    val registeredThemes: Collection<String?>
        get() = ArrayList(REGISTERED_THEMES.keys)

    val defaultTheme: Theme?
        get() = REGISTERED_THEMES["default"]

    init {
        val defaultThemeProperties = loadPropTheme("default-theme.properties")
        if (defaultThemeProperties != null) {
            registerPropTheme("default", defaultThemeProperties)
        } else {
            registerTheme("default", DefaultTheme())
        }

        registerPropTheme("bigsnake", loadPropTheme("bigsnake-theme.properties"))
        registerPropTheme("businessmachine", loadPropTheme("businessmachine-theme.properties"))
        registerPropTheme("conqueror", loadPropTheme("conqueror-theme.properties"))
        registerPropTheme("defrost", loadPropTheme("defrost-theme.properties"))
        registerPropTheme("blaster", loadPropTheme("blaster-theme.properties"))
    }

    fun getRegisteredTheme(name: String?): Theme? = REGISTERED_THEMES[name]

    fun registerTheme(name: String?, theme: Theme?) {
        if (theme == null) {
            throw IllegalArgumentException("Theme cannot be null")
        }
        if (name.isNullOrEmpty()) {
            throw IllegalArgumentException("Name cannot be empty")
        }
        val result = REGISTERED_THEMES.putIfAbsent(name, theme)
        if (result != null && result !== theme) {
            throw IllegalArgumentException("There is already a theme registered with the name '$name'")
        }
    }

    private fun registerPropTheme(name: String?, properties: Properties?) {
        if (properties != null) {
            registerTheme(name, PropertyTheme(properties, false))
        }
    }

    private fun loadPropTheme(resourceFileName: String): Properties? {
        val properties = Properties()
        return try {
            val classLoader = AbstractTextGUI::class.java.classLoader
            val resourceAsStream = classLoader.getResourceAsStream(resourceFileName)
                ?: FileInputStream("src/main/resources/$resourceFileName")
            resourceAsStream.use { stream ->
                properties.load(stream)
            }
            properties
        } catch (_: IOException) {
            null
        }
    }
}
