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
import com.googlecode.lanterna.gui2.Button
import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.ComponentRenderer
import com.googlecode.lanterna.gui2.WindowDecorationRenderer
import com.googlecode.lanterna.gui2.WindowPostRenderer
import com.googlecode.lanterna.gui2.WindowShadowRenderer
import java.util.ArrayList
import java.util.Collections
import java.util.EnumSet
import java.util.HashMap
import java.util.LinkedList
import java.util.regex.Pattern

abstract class AbstractTheme protected constructor(
    postRenderer: WindowPostRenderer?,
    decorationRenderer: WindowDecorationRenderer?
) : Theme {
    private val rootNode: ThemeTreeNode
    private val windowPostRenderer: WindowPostRenderer?
    private val windowDecorationRenderer: WindowDecorationRenderer?

    init {
        this.rootNode = ThemeTreeNode(Any::class.java, null)
        this.windowPostRenderer = postRenderer
        this.windowDecorationRenderer = decorationRenderer

        rootNode.foregroundMap[STYLE_NORMAL] = TextColor.ANSI.WHITE
        rootNode.backgroundMap[STYLE_NORMAL] = TextColor.ANSI.BLACK
        classloadStandardRenderersForGraal()
    }

    private fun classloadStandardRenderersForGraal() {
        WindowShadowRenderer::class.java.toString()
        Button.DefaultButtonRenderer::class.java.toString()
        Button.FlatButtonRenderer::class.java.toString()
        Button.BorderedButtonRenderer::class.java.toString()
    }

    protected open fun addStyle(definition: String?, style: String, value: String): Boolean {
        val node = getNode(definition) ?: return false
        node.apply(style, value)
        return true
    }

    private fun getNode(definition: String?): ThemeTreeNode? {
        return try {
            if (definition == null || definition.trim().isEmpty()) {
                getNode(Any::class.java)
            } else {
                getNode(Class.forName(definition))
            }
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    private fun getNode(definition: Class<*>): ThemeTreeNode {
        if (definition == Any::class.java) {
            return rootNode
        }
        val parent = getNode(definition.superclass ?: throw NullPointerException())
        if (parent.childMap.containsKey(definition)) {
            return parent.childMap[definition] ?: throw NullPointerException()
        }

        val node = ThemeTreeNode(definition, parent)
        parent.childMap[definition] = node
        return node
    }

    override open fun getDefaultDefinition(): ThemeDefinition {
        return DefinitionImpl(rootNode)
    }

    override open fun getDefinition(clazz: Class<*>?): ThemeDefinition {
        val hierarchy = LinkedList<Class<*>>()
        var current = clazz
        while (current != null && current != Any::class.java) {
            hierarchy.addFirst(current)
            current = current.superclass
        }

        var node = rootNode
        for (aClass in hierarchy) {
            if (node.childMap.containsKey(aClass)) {
                node = node.childMap[aClass] ?: throw NullPointerException()
            } else {
                break
            }
        }
        return DefinitionImpl(node)
    }

    override open fun getWindowPostRenderer(): WindowPostRenderer? {
        return windowPostRenderer
    }

    override open fun getWindowDecorationRenderer(): WindowDecorationRenderer? {
        return windowDecorationRenderer
    }

    open fun findRedundantDeclarations(): List<String> {
        val result: MutableList<String> = ArrayList()
        for (node in rootNode.childMap.values) {
            findRedundantDeclarations(result, node)
        }
        Collections.sort(result)
        return result
    }

    private fun findRedundantDeclarations(result: MutableList<String>, node: ThemeTreeNode) {
        for (style in node.foregroundMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val color = node.foregroundMap[style]
            val colorFromParent = StyleImpl(node.parent ?: throw NullPointerException(), style).foreground
            if ((color ?: throw NullPointerException()).equals(colorFromParent)) {
                result.add(node.clazz.name + ".foreground" + formattedStyle)
            }
        }
        for (style in node.backgroundMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val color = node.backgroundMap[style]
            val colorFromParent = StyleImpl(node.parent ?: throw NullPointerException(), style).background
            if ((color ?: throw NullPointerException()).equals(colorFromParent)) {
                result.add(node.clazz.name + ".background" + formattedStyle)
            }
        }
        for (style in node.sgrMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val sgrs = node.sgrMap[style]
            val sgrsFromParent = StyleImpl(node.parent ?: throw NullPointerException(), style).sgRs
            if ((sgrs ?: throw NullPointerException()).equals(sgrsFromParent)) {
                result.add(node.clazz.name + ".sgr" + formattedStyle)
            }
        }

        for (childNode in node.childMap.values) {
            findRedundantDeclarations(result, childNode)
        }
    }

    private inner class DefinitionImpl(val node: ThemeTreeNode) : ThemeDefinition {
        override fun getNormal(): ThemeStyle {
            return StyleImpl(node, STYLE_NORMAL)
        }

        override fun getPreLight(): ThemeStyle {
            return StyleImpl(node, STYLE_PRELIGHT)
        }

        override fun getSelected(): ThemeStyle {
            return StyleImpl(node, STYLE_SELECTED)
        }

        override fun getActive(): ThemeStyle {
            return StyleImpl(node, STYLE_ACTIVE)
        }

        override fun getInsensitive(): ThemeStyle {
            return StyleImpl(node, STYLE_INSENSITIVE)
        }

        override fun getCustom(name: String): ThemeStyle {
            return StyleImpl(node, name)
        }

        override fun getCustom(name: String, defaultValue: ThemeStyle): ThemeStyle {
            var customStyle: ThemeStyle? = getCustom(name)
            if (customStyle == null) {
                customStyle = defaultValue
            }
            return customStyle
        }

        override fun getCharacter(name: String, fallback: Char): Char {
            val character = node.characterMap[name]
            if (character == null) {
                return if (node == rootNode) {
                    fallback
                } else {
                    DefinitionImpl(node.parent ?: throw NullPointerException()).getCharacter(name, fallback)
                }
            }
            return character
        }

        override fun isCursorVisible(): Boolean {
            val cursorVisible = node.cursorVisible
            if (cursorVisible == null) {
                return if (node == rootNode) {
                    true
                } else {
                    DefinitionImpl(node.parent ?: throw NullPointerException()).isCursorVisible
                }
            }
            return cursorVisible
        }

        override fun getIntegerProperty(name: String, defaultValue: Int): Int {
            val propertyValue = node.propertyMap[name]
            if (propertyValue == null) {
                return if (node == rootNode) {
                    defaultValue
                } else {
                    DefinitionImpl(node.parent ?: throw NullPointerException()).getIntegerProperty(name, defaultValue)
                }
            }
            return propertyValue.toInt()
        }

        override fun getBooleanProperty(name: String, defaultValue: Boolean): Boolean {
            val propertyValue = node.propertyMap[name]
            if (propertyValue == null) {
                return if (node == rootNode) {
                    defaultValue
                } else {
                    DefinitionImpl(node.parent ?: throw NullPointerException()).getBooleanProperty(name, defaultValue)
                }
            }
            return propertyValue.toBoolean()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Component> getRenderer(type: Class<T>): ComponentRenderer<T>? {
            val rendererClass = node.renderer
            if (rendererClass == null) {
                return if (node == rootNode) {
                    null
                } else {
                    DefinitionImpl(node.parent ?: throw NullPointerException()).getRenderer(type)
                }
            }
            return instanceByClassName(rendererClass) as ComponentRenderer<T>?
        }
    }

    private inner class StyleImpl(
        private val styleNode: ThemeTreeNode,
        private val name: String
    ) : ThemeStyle {
        override fun getForeground(): TextColor {
            var node: ThemeTreeNode? = styleNode
            while (node != null) {
                if (node.foregroundMap.containsKey(name)) {
                    return node.foregroundMap[name] ?: throw NullPointerException()
                }
                node = node.parent
            }
            var fallback = rootNode.foregroundMap[STYLE_NORMAL]
            if (fallback == null) {
                fallback = TextColor.ANSI.WHITE
            }
            return fallback
        }

        override fun getBackground(): TextColor {
            var node: ThemeTreeNode? = styleNode
            while (node != null) {
                if (node.backgroundMap.containsKey(name)) {
                    return node.backgroundMap[name] ?: throw NullPointerException()
                }
                node = node.parent
            }
            var fallback = rootNode.backgroundMap[STYLE_NORMAL]
            if (fallback == null) {
                fallback = TextColor.ANSI.BLACK
            }
            return fallback
        }

        override fun getSGRs(): EnumSet<SGR> {
            var node: ThemeTreeNode? = styleNode
            while (node != null) {
                if (node.sgrMap.containsKey(name)) {
                    return EnumSet.copyOf(node.sgrMap[name] ?: throw NullPointerException())
                }
                node = node.parent
            }
            var fallback = rootNode.sgrMap[STYLE_NORMAL]
            if (fallback == null) {
                fallback = EnumSet.noneOf(SGR::class.java)
            }
            return EnumSet.copyOf(fallback)
        }
    }

    private class ThemeTreeNode(
        val clazz: Class<*>,
        val parent: ThemeTreeNode?
    ) {
        val childMap: MutableMap<Class<*>, ThemeTreeNode> = HashMap()
        val foregroundMap: MutableMap<String, TextColor> = HashMap()
        val backgroundMap: MutableMap<String, TextColor> = HashMap()
        val sgrMap: MutableMap<String, EnumSet<SGR>> = HashMap()
        val characterMap: MutableMap<String, Char> = HashMap()
        val propertyMap: MutableMap<String, String?> = HashMap()
        var cursorVisible: Boolean? = true
        var renderer: String? = null

        fun apply(style: String, value: String) {
            var mutableValue = value.trim()
            val matcher = STYLE_FORMAT.matcher(style)
            if (!matcher.matches()) {
                throw IllegalArgumentException("Unknown style declaration: $style")
            }
            val styleComponent = matcher.group(1)
            val group = if (matcher.groupCount() > 2) matcher.group(3) else null
            when (styleComponent.lowercase().trim()) {
                "foreground" -> foregroundMap[getCategory(group)] = parseValue(mutableValue)
                "background" -> backgroundMap[getCategory(group)] = parseValue(mutableValue)
                "sgr" -> sgrMap[getCategory(group)] = parseSGR(mutableValue)
                "char" -> characterMap[getCategory(group)] = if (mutableValue.isEmpty()) ' ' else mutableValue[0]
                "cursor" -> cursorVisible = mutableValue.toBoolean()
                "property" -> propertyMap[getCategory(group)] = if (mutableValue.isEmpty()) null else mutableValue.trim()
                "renderer" -> renderer = if (mutableValue.trim().isEmpty()) null else mutableValue.trim()
                "postrenderer", "windowdecoration" -> {
                    // Don't do anything with this now, we might use it later
                }
                else -> throw IllegalArgumentException("Unknown style component \"$styleComponent\" in style \"$style\"")
            }
        }

        private fun parseValue(value: String): TextColor {
            return TextColor.Factory.fromString(value)
        }

        private fun parseSGR(value: String): EnumSet<SGR> {
            var mutableValue = value.trim()
            val sgrEntries = mutableValue.split(",".toRegex()).toTypedArray()
            val sgrSet = EnumSet.noneOf(SGR::class.java)
            for (entry0 in sgrEntries) {
                var entry = entry0
                entry = entry.trim().uppercase()
                if (entry.isNotEmpty()) {
                    try {
                        sgrSet.add(SGR.valueOf(entry))
                    } catch (e: IllegalArgumentException) {
                        throw IllegalArgumentException("Unknown SGR code \"$entry\"", e)
                    }
                }
            }
            return sgrSet
        }

        private fun getCategory(group: String?): String {
            if (group == null) {
                return STYLE_NORMAL
            }
            for (style in java.util.Arrays.asList(
                STYLE_ACTIVE,
                STYLE_INSENSITIVE,
                STYLE_PRELIGHT,
                STYLE_NORMAL,
                STYLE_SELECTED
            )) {
                if (group.uppercase() == style) {
                    return style
                }
            }
            return group
        }
    }

    companion object {
        private const val STYLE_NORMAL = ""
        private const val STYLE_PRELIGHT = "PRELIGHT"
        private const val STYLE_SELECTED = "SELECTED"
        private const val STYLE_ACTIVE = "ACTIVE"
        private const val STYLE_INSENSITIVE = "INSENSITIVE"
        private val STYLE_FORMAT = Pattern.compile("([a-zA-Z]+)(\\[([a-zA-Z0-9-_]+)])?")

        @JvmStatic
        protected fun instanceByClassName(className: String?): Any? {
            if (className == null || className.trim().isEmpty()) {
                return null
            }
            return try {
                Class.forName(className).newInstance()
            } catch (e: InstantiationException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            }
        }
    }
}
