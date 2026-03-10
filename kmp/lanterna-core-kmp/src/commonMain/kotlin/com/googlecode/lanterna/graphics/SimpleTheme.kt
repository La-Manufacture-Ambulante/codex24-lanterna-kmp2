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
import com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer
import com.googlecode.lanterna.gui2.GUIBackdrop
import com.googlecode.lanterna.gui2.RadioBoxList
import com.googlecode.lanterna.gui2.TextBox
import com.googlecode.lanterna.gui2.TextGUI
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import com.googlecode.lanterna.gui2.WindowShadowRenderer
import com.googlecode.lanterna.gui2.table.Table
import java.util.EnumSet
import java.util.HashMap
import java.util.Map
import java.util.Properties

/**
 * Very basic implementation of [Theme].
 */
class SimpleTheme(foreground: TextColor?, background: TextColor?, vararg styles: SGR?) : Theme {
    @get:Synchronized
    override val defaultDefinition: Definition = Definition(DefaultMutableThemeStyle(foreground, background, *styles))

    private val overrideDefinitions: MutableMap<Class<*>, Definition> = HashMap()

    @get:Synchronized
    @set:Synchronized
    override var windowPostRenderer: WindowPostRenderer? = null

    @get:Synchronized
    @set:Synchronized
    override var windowDecorationRenderer: WindowDecorationRenderer? = null

    @Synchronized
    override fun getDefinition(clazz: Class<*>?): Definition {
        val resolved = if (clazz != null) overrideDefinitions[clazz] else null
        return resolved ?: defaultDefinition
    }

    /**
     * Adds or replaces a definition override for [clazz].
     */
    @Synchronized
    fun addOverride(clazz: Class<*>?, foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
        val definition = Definition(DefaultMutableThemeStyle(foreground, background, *styles))
        if (clazz != null) {
            overrideDefinitions[clazz] = definition
        }
        return definition
    }

    @Synchronized
    fun setWindowPostRenderer(windowPostRenderer: WindowPostRenderer?): SimpleTheme {
        this.windowPostRenderer = windowPostRenderer
        return this
    }

    @Synchronized
    fun setWindowDecorationRenderer(windowDecorationRenderer: WindowDecorationRenderer?): SimpleTheme {
        this.windowDecorationRenderer = windowDecorationRenderer
        return this
    }

    interface RendererProvider<T : Component?> {
        fun getRenderer(type: Class<T?>?): ComponentRenderer<T?>?
    }

    /**
     * Mutable [ThemeDefinition] used by [SimpleTheme].
     */
    class Definition constructor(override val normal: ThemeStyle?) : ThemeDefinition {
        private var preLightBacking: ThemeStyle? = null
        private var selectedBacking: ThemeStyle? = null
        private var activeBacking: ThemeStyle? = null
        private var insensitiveBacking: ThemeStyle? = null
        private val customStyles: MutableMap<String?, ThemeStyle?> = HashMap()
        private val properties = Properties()
        private val characterMap: MutableMap<String?, Char> = HashMap()
        private val componentRendererMap: MutableMap<Class<*>, RendererProvider<*>> = HashMap()
        private var cursorVisible: Boolean = true

        @get:Synchronized
        override val preLight: ThemeStyle?
            get() = preLightBacking ?: normal

        @Synchronized
        fun setPreLight(foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
            preLightBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        @get:Synchronized
        override val selected: ThemeStyle?
            get() = selectedBacking ?: normal

        @Synchronized
        fun setSelected(foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
            selectedBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        @get:Synchronized
        override val active: ThemeStyle?
            get() = activeBacking ?: normal

        @Synchronized
        fun setActive(foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
            activeBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        @get:Synchronized
        override val insensitive: ThemeStyle?
            get() = insensitiveBacking ?: normal

        @Synchronized
        fun setInsensitive(foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
            insensitiveBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        @Synchronized
        override fun getCustom(name: String?): ThemeStyle? {
            return customStyles[name]
        }

        @Synchronized
        override fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle? {
            return customStyles[name] ?: defaultValue
        }

        @Synchronized
        fun setCustom(name: String?, foreground: TextColor?, background: TextColor?, vararg styles: SGR?): Definition {
            customStyles[name] = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        @Synchronized
        override fun getIntegerProperty(name: String?, defaultValue: Int): Int {
            return Integer.parseInt(properties.getProperty(name, Integer.toString(defaultValue)))
        }

        @Synchronized
        fun setIntegerProperty(name: String?, value: Int): Definition {
            properties.setProperty(name, Integer.toString(value))
            return this
        }

        @Synchronized
        override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
            return java.lang.Boolean.parseBoolean(properties.getProperty(name, java.lang.Boolean.toString(defaultValue)))
        }

        @Synchronized
        fun setBooleanProperty(name: String?, value: Boolean): Definition {
            properties.setProperty(name, java.lang.Boolean.toString(value))
            return this
        }

        @get:Synchronized
        override val isCursorVisible: Boolean
            get() = cursorVisible

        @Synchronized
        fun setCursorVisible(cursorVisible: Boolean): Definition {
            this.cursorVisible = cursorVisible
            return this
        }

        @Synchronized
        override fun getCharacter(name: String?, fallback: Char): Char {
            return characterMap[name] ?: fallback
        }

        @Synchronized
        fun setCharacter(name: String?, character: Char): Definition {
            characterMap[name] = character
            return this
        }

        @Suppress("UNCHECKED_CAST")
        @Synchronized
        override fun <T : Component?> getRenderer(type: Class<T?>?): ComponentRenderer<T?>? {
            if (type == null) {
                return null
            }
            val rendererProvider = componentRendererMap[type] as RendererProvider<T?>?
            return rendererProvider?.getRenderer(type)
        }

        @Synchronized
        fun <T : Component?> setRenderer(type: Class<T?>?, rendererProvider: RendererProvider<T?>?): Definition {
            if (type == null) {
                return this
            }
            if (rendererProvider == null) {
                componentRendererMap.remove(type)
            } else {
                componentRendererMap[type] = rendererProvider
            }
            return this
        }

    }

    companion object {
        /**
         * Creates a preconfigured [SimpleTheme] similar to Lanterna's default simple style setup.
         */
        fun makeTheme(
            activeIsBold: Boolean,
            baseForeground: TextColor?,
            baseBackground: TextColor?,
            editableForeground: TextColor?,
            editableBackground: TextColor?,
            selectedForeground: TextColor?,
            selectedBackground: TextColor?,
            guiBackground: TextColor?,
        ): SimpleTheme {
            val activeStyle = if (activeIsBold) arrayOf(SGR.BOLD) else emptyArray()

            val theme = SimpleTheme(baseForeground, baseBackground)
            theme.defaultDefinition.setSelected(baseBackground, baseForeground, *activeStyle)
            theme.defaultDefinition.setActive(selectedForeground, selectedBackground, *activeStyle)

            theme.addOverride(AbstractBorder::class.java, baseForeground, baseBackground)
                .setSelected(baseForeground, baseBackground, *activeStyle)
            theme.addOverride(AbstractListBox::class.java, baseForeground, baseBackground)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(Button::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(CheckBox::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
                .setPreLight(selectedForeground, selectedBackground, *activeStyle)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(CheckBoxList::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(ComboBox::class.java, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setPreLight(editableForeground, editableBackground)
            theme.addOverride(DefaultWindowDecorationRenderer::class.java, baseForeground, baseBackground)
                .setActive(baseForeground, baseBackground, *activeStyle)
            theme.addOverride(GUIBackdrop::class.java, baseForeground, guiBackground)
            theme.addOverride(RadioBoxList::class.java, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(Table::class.java, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setSelected(baseForeground, baseBackground)
            theme.addOverride(TextBox::class.java, editableForeground, editableBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setSelected(editableForeground, editableBackground, *activeStyle)

            theme.setWindowPostRenderer(WindowShadowRenderer())
            return theme
        }
    }
}
