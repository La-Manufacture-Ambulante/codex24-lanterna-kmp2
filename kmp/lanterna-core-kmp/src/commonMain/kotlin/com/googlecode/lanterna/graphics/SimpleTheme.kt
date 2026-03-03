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
import com.googlecode.lanterna.gui2.AbstractBorder
import com.googlecode.lanterna.gui2.AbstractListBox
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.CheckBox
import com.googlecode.lanterna.gui2.CheckBoxList
import com.googlecode.lanterna.gui2.ComboBox
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.DefaultMutableThemeStyle
import com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer
import com.googlecode.lanterna.gui2.GUIBackdrop
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.TextGUI
import com.googlecode.lanterna.gui2.Theme
import com.googlecode.lanterna.gui2.ThemeDefinition
import com.googlecode.lanterna.gui2.ThemeStyle
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import com.googlecode.lanterna.gui2.WindowShadowRenderer
import com.googlecode.lanterna.gui2.table.Table
import java.util.HashMap
import java.util.Properties

private fun createThemeStyle(
    foreground: TextColor?,
    background: TextColor?,
    styles: Array<out SGR?>?
): ThemeStyle {
    return if (styles == null) {
        DefaultMutableThemeStyle(foreground, background, null as Array<out SGR?>?)
    } else {
        DefaultMutableThemeStyle(foreground, background, *styles)
    }
}

open class SimpleTheme(
    foreground: TextColor?,
    background: TextColor?,
    styles: Array<out SGR?>? = emptyArray()
) : Theme {
    private val defaultDefinition: Definition = Definition(createThemeStyle(foreground, background, styles))
    private val overrideDefinitions: MutableMap<Class<*>?, Definition> = HashMap()
    private var windowPostRenderer: WindowPostRenderer? = null
    private var windowDecorationRenderer: WindowDecorationRenderer? = null

    @Synchronized
    open override fun getDefaultDefinition(): Definition {
        return defaultDefinition
    }

    @Synchronized
    open override fun getDefinition(clazz: Class<*>?): Definition {
        val definition = overrideDefinitions[clazz]
        if (definition == null) {
            return getDefaultDefinition()
        }
        return definition
    }

    @Synchronized
    open fun addOverride(
        clazz: Class<*>?,
        foreground: TextColor?,
        background: TextColor?,
        styles: Array<out SGR?>? = emptyArray()
    ): Definition {
        val definition = Definition(createThemeStyle(foreground, background, styles))
        overrideDefinitions[clazz] = definition
        return definition
    }

    @Synchronized
    open override fun getWindowPostRenderer(): WindowPostRenderer? {
        return windowPostRenderer
    }

    @Synchronized
    open fun setWindowPostRenderer(windowPostRenderer: WindowPostRenderer?): SimpleTheme {
        this.windowPostRenderer = windowPostRenderer
        return this
    }

    @Synchronized
    open override fun getWindowDecorationRenderer(): WindowDecorationRenderer? {
        return windowDecorationRenderer
    }

    @Synchronized
    open fun setWindowDecorationRenderer(windowDecorationRenderer: WindowDecorationRenderer?): SimpleTheme {
        this.windowDecorationRenderer = windowDecorationRenderer
        return this
    }

    interface RendererProvider<T : Component> {
        fun getRenderer(type: Class<T>?): ComponentRenderer<T>?
    }

    open class Definition private constructor(private val normal: ThemeStyle) : ThemeDefinition {
        private var preLight: ThemeStyle? = null
        private var selected: ThemeStyle? = null
        private var active: ThemeStyle? = null
        private var insensitive: ThemeStyle? = null
        private val customStyles: MutableMap<String?, ThemeStyle> = HashMap()
        private val properties: Properties = Properties()
        private val characterMap: MutableMap<String?, Char> = HashMap()
        private val componentRendererMap: MutableMap<Class<*>?, RendererProvider<*>> = HashMap()
        private var cursorVisible: Boolean = true

        @Synchronized
        open override fun getNormal(): ThemeStyle {
            return normal
        }

        @Synchronized
        open override fun getPreLight(): ThemeStyle {
            if (preLight == null) {
                return normal
            }
            return preLight as ThemeStyle
        }

        @Synchronized
        open fun setPreLight(
            foreground: TextColor?,
            background: TextColor?,
            styles: Array<out SGR?>? = emptyArray()
        ): Definition {
            this.preLight = createThemeStyle(foreground, background, styles)
            return this
        }

        @Synchronized
        open override fun getSelected(): ThemeStyle {
            if (selected == null) {
                return normal
            }
            return selected as ThemeStyle
        }

        @Synchronized
        open fun setSelected(
            foreground: TextColor?,
            background: TextColor?,
            styles: Array<out SGR?>? = emptyArray()
        ): Definition {
            this.selected = createThemeStyle(foreground, background, styles)
            return this
        }

        @Synchronized
        open override fun getActive(): ThemeStyle {
            if (active == null) {
                return normal
            }
            return active as ThemeStyle
        }

        @Synchronized
        open fun setActive(
            foreground: TextColor?,
            background: TextColor?,
            styles: Array<out SGR?>? = emptyArray()
        ): Definition {
            this.active = createThemeStyle(foreground, background, styles)
            return this
        }

        @Synchronized
        open override fun getInsensitive(): ThemeStyle {
            if (insensitive == null) {
                return normal
            }
            return insensitive as ThemeStyle
        }

        @Synchronized
        open fun setInsensitive(
            foreground: TextColor?,
            background: TextColor?,
            styles: Array<out SGR?>? = emptyArray()
        ): Definition {
            this.insensitive = createThemeStyle(foreground, background, styles)
            return this
        }

        @Synchronized
        open override fun getCustom(name: String?): ThemeStyle? {
            return customStyles[name]
        }

        @Synchronized
        open override fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle? {
            val themeStyle = customStyles[name]
            if (themeStyle == null) {
                return defaultValue
            }
            return themeStyle
        }

        @Synchronized
        open fun setCustom(
            name: String?,
            foreground: TextColor?,
            background: TextColor?,
            styles: Array<out SGR?>? = emptyArray()
        ): Definition {
            customStyles[name] = createThemeStyle(foreground, background, styles)
            return this
        }

        @Synchronized
        open fun getIntegerProperty(name: String?, defaultValue: Int): Int {
            return Integer.parseInt(properties.getProperty(name, Integer.toString(defaultValue)))
        }

        @Synchronized
        open fun setIntegerProperty(name: String?, value: Int): Definition {
            properties.setProperty(name, Integer.toString(value))
            return this
        }

        @Synchronized
        open override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
            return java.lang.Boolean.parseBoolean(properties.getProperty(name, java.lang.Boolean.toString(defaultValue)))
        }

        @Synchronized
        open fun setBooleanProperty(name: String?, value: Boolean): Definition {
            properties.setProperty(name, java.lang.Boolean.toString(value))
            return this
        }

        @Synchronized
        open override fun isCursorVisible(): Boolean {
            return cursorVisible
        }

        @Synchronized
        open fun setCursorVisible(cursorVisible: Boolean): Definition {
            this.cursorVisible = cursorVisible
            return this
        }

        @Synchronized
        open override fun getCharacter(name: String?, fallback: Char): Char {
            val character = characterMap[name]
            if (character == null) {
                return fallback
            }
            return character
        }

        @Synchronized
        open fun setCharacter(name: String?, character: Char): Definition {
            characterMap[name] = character
            return this
        }

        @Suppress("UNCHECKED_CAST")
        @Synchronized
        open override fun <T : Component> getRenderer(type: Class<T>?): ComponentRenderer<T>? {
            val rendererProvider = componentRendererMap[type] as RendererProvider<T>?
            if (rendererProvider == null) {
                return null
            }
            return rendererProvider.getRenderer(type)
        }

        @Synchronized
        open fun <T : Component> setRenderer(
            type: Class<T>?,
            rendererProvider: RendererProvider<T>?
        ): Definition {
            if (rendererProvider == null) {
                componentRendererMap.remove(type)
            } else {
                componentRendererMap[type] = rendererProvider
            }
            return this
        }
    }

    companion object {
        @JvmStatic
        fun makeTheme(
            activeIsBold: Boolean,
            baseForeground: TextColor?,
            baseBackground: TextColor?,
            editableForeground: TextColor?,
            editableBackground: TextColor?,
            selectedForeground: TextColor?,
            selectedBackground: TextColor?,
            guiBackground: TextColor?
        ): SimpleTheme {
            val activeStyle: Array<SGR?> = if (activeIsBold) arrayOf(SGR.BOLD) else emptyArray()

            val theme = SimpleTheme(baseForeground, baseBackground)
            theme.getDefaultDefinition().setSelected(baseBackground, baseForeground, activeStyle)
            theme.getDefaultDefinition().setActive(selectedForeground, selectedBackground, activeStyle)

            theme.addOverride(AbstractBorder::class.java, baseForeground, baseBackground)
                .setSelected(baseForeground, baseBackground, activeStyle)
            theme.addOverride(AbstractListBox::class.java, baseForeground, baseBackground)
                .setSelected(selectedForeground, selectedBackground, activeStyle)
            theme.addOverride(Button::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, activeStyle)
                .setSelected(selectedForeground, selectedBackground, activeStyle)
            theme.addOverride(CheckBox::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, activeStyle)
                .setPreLight(selectedForeground, selectedBackground, activeStyle)
                .setSelected(selectedForeground, selectedBackground, activeStyle)
            theme.addOverride(CheckBoxList::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, activeStyle)
            theme.addOverride(ComboBox::class.java, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, activeStyle)
                .setPreLight(editableForeground, editableBackground)
            theme.addOverride(DefaultWindowDecorationRenderer::class.java, baseForeground, baseBackground)
                .setActive(baseForeground, baseBackground, activeStyle)
            theme.addOverride(GUIBackdrop::class.java, baseForeground, guiBackground)
            theme.addOverride(RadioBoxList::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, activeStyle)
            theme.addOverride(Table::class.java, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, activeStyle)
                .setSelected(baseForeground, baseBackground)
            theme.addOverride(TextBox::class.java, editableForeground, editableBackground)
                .setActive(editableForeground, editableBackground, activeStyle)
                .setSelected(editableForeground, editableBackground, activeStyle)

            theme.setWindowPostRenderer(WindowShadowRenderer())

            return theme
        }
    }
}
