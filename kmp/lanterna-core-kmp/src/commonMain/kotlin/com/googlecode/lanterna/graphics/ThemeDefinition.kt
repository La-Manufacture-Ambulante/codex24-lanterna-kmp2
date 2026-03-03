package com.googlecode.lanterna.graphics

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer

interface ThemeDefinition {
    fun getNormal(): ThemeStyle?

    fun getPreLight(): ThemeStyle?

    fun getSelected(): ThemeStyle?

    fun getActive(): ThemeStyle?

    fun getInsensitive(): ThemeStyle?

    fun getCustom(name: String?): ThemeStyle?

    fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle?

    fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean

    fun getIntegerProperty(name: String?, defaultValue: Int): Int

    fun isCursorVisible(): Boolean

    fun getCharacter(name: String?, fallback: Char): Char

    fun <T : Component> getRenderer(type: Class<T>?): ComponentRenderer<T>?
}
