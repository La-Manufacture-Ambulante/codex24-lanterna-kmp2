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
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import com.googlecode.lanterna.gui2.WindowShadowRenderer
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.internal.compat.Properties
import kotlin.collections.HashMap
import kotlin.reflect.KClass

/**
 * Very basic implementation of [Theme].
 */
class SimpleTheme(foreground: TextColor?, background: TextColor?, vararg styles: SGR?) : Theme {
    override val defaultDefinition: Definition = Definition(DefaultMutableThemeStyle(foreground, background, *styles))

    private val overrideDefinitions: MutableMap<KClass<*>, Definition> = HashMap()

    override var windowPostRenderer: WindowPostRenderer? = null

    override var windowDecorationRenderer: WindowDecorationRenderer? = null

    override fun getDefinition(clazz: KClass<*>?): Definition {
        val resolved = if (clazz != null) overrideDefinitions[clazz] else null
        return resolved ?: defaultDefinition
    }

    fun addOverride(
        clazz: KClass<*>?,
        foreground: TextColor?,
        background: TextColor?,
        vararg styles: SGR?,
    ): Definition {
        val definition = Definition(DefaultMutableThemeStyle(foreground, background, *styles))
        if (clazz != null) {
            overrideDefinitions[clazz] = definition
        }
        return definition
    }

    fun setWindowPostRenderer(windowPostRenderer: WindowPostRenderer?): SimpleTheme {
        this.windowPostRenderer = windowPostRenderer
        return this
    }

    fun setWindowDecorationRenderer(windowDecorationRenderer: WindowDecorationRenderer?): SimpleTheme {
        this.windowDecorationRenderer = windowDecorationRenderer
        return this
    }

    interface RendererProvider<T : Component> {
        fun getRenderer(type: KClass<T>?): ComponentRenderer<T?>?
    }

    class Definition constructor(override val normal: ThemeStyle?) : ThemeDefinition {
        private var preLightBacking: ThemeStyle? = null
        private var selectedBacking: ThemeStyle? = null
        private var activeBacking: ThemeStyle? = null
        private var insensitiveBacking: ThemeStyle? = null
        private val customStyles: MutableMap<String?, ThemeStyle?> = HashMap()
        private val properties = Properties()
        private val characterMap: MutableMap<String?, Char> = HashMap()
        private val componentRendererMap: MutableMap<KClass<*>, RendererProvider<*>> = HashMap()
        private var cursorVisible: Boolean = true

        override val preLight: ThemeStyle?
            get() = preLightBacking ?: normal

        fun setPreLight(
            foreground: TextColor?,
            background: TextColor?,
            vararg styles: SGR?,
        ): Definition {
            preLightBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        override val selected: ThemeStyle?
            get() = selectedBacking ?: normal

        fun setSelected(
            foreground: TextColor?,
            background: TextColor?,
            vararg styles: SGR?,
        ): Definition {
            selectedBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        override val active: ThemeStyle?
            get() = activeBacking ?: normal

        fun setActive(
            foreground: TextColor?,
            background: TextColor?,
            vararg styles: SGR?,
        ): Definition {
            activeBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        override val insensitive: ThemeStyle?
            get() = insensitiveBacking ?: normal

        fun setInsensitive(
            foreground: TextColor?,
            background: TextColor?,
            vararg styles: SGR?,
        ): Definition {
            insensitiveBacking = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        override fun getCustom(name: String?): ThemeStyle? {
            return customStyles[name]
        }

        override fun getCustom(
            name: String?,
            defaultValue: ThemeStyle?,
        ): ThemeStyle? {
            return customStyles[name] ?: defaultValue
        }

        fun setCustom(
            name: String?,
            foreground: TextColor?,
            background: TextColor?,
            vararg styles: SGR?,
        ): Definition {
            customStyles[name] = DefaultMutableThemeStyle(foreground, background, *styles)
            return this
        }

        override fun getIntegerProperty(
            name: String?,
            defaultValue: Int,
        ): Int {
            return com.googlecode.lanterna.internal.compat.Integer.parseInt(
                properties.getProperty(name, com.googlecode.lanterna.internal.compat.Integer.toString(defaultValue)),
            )
        }

        fun setIntegerProperty(
            name: String?,
            value: Int,
        ): Definition {
            properties.setProperty(name, com.googlecode.lanterna.internal.compat.Integer.toString(value))
            return this
        }

        override fun getBooleanProperty(
            name: String?,
            defaultValue: Boolean,
        ): Boolean {
            return com.googlecode.lanterna.internal.compat.JBoolean.parseBoolean(
                properties.getProperty(name, com.googlecode.lanterna.internal.compat.JBoolean.toString(defaultValue)),
            )
        }

        fun setBooleanProperty(
            name: String?,
            value: Boolean,
        ): Definition {
            properties.setProperty(name, com.googlecode.lanterna.internal.compat.JBoolean.toString(value))
            return this
        }

        override val isCursorVisible: Boolean
            get() = cursorVisible

        fun setCursorVisible(cursorVisible: Boolean): Definition {
            this.cursorVisible = cursorVisible
            return this
        }

        override fun getCharacter(
            name: String?,
            fallback: Char,
        ): Char {
            return characterMap[name] ?: fallback
        }

        fun setCharacter(
            name: String?,
            character: Char,
        ): Definition {
            characterMap[name] = character
            return this
        }

        override fun <T : Component> getRenderer(type: KClass<T>?): ComponentRenderer<T?>? {
            if (type == null) {
                return null
            }
            val rendererProvider = componentRendererMap[type] ?: return null
            val typedProvider = rendererProvider as? RendererProvider<T> ?: return null
            return typedProvider.getRenderer(type)
        }

        fun <T : Component> setRenderer(
            type: KClass<T>?,
            rendererProvider: RendererProvider<T>?,
        ): Definition {
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

            theme.addOverride(AbstractBorder::class, baseForeground, baseBackground)
                .setSelected(baseForeground, baseBackground, *activeStyle)
            theme.addOverride(AbstractListBox::class, baseForeground, baseBackground)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(Button::class, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(CheckBox::class, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
                .setPreLight(selectedForeground, selectedBackground, *activeStyle)
                .setSelected(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(CheckBoxList::class, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(ComboBox::class, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setPreLight(editableForeground, editableBackground)
            theme.addOverride(DefaultWindowDecorationRenderer::class, baseForeground, baseBackground)
                .setActive(baseForeground, baseBackground, *activeStyle)
            theme.addOverride(GUIBackdrop::class, baseForeground, guiBackground)
            theme.addOverride(RadioBoxList::class, baseForeground, baseBackground)
                .setActive(selectedForeground, selectedBackground, *activeStyle)
            theme.addOverride(Table::class, baseForeground, baseBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setSelected(baseForeground, baseBackground)
            theme.addOverride(TextBox::class, editableForeground, editableBackground)
                .setActive(editableForeground, editableBackground, *activeStyle)
                .setSelected(editableForeground, editableBackground, *activeStyle)

            theme.setWindowPostRenderer(WindowShadowRenderer())
            return theme
        }
    }
}
