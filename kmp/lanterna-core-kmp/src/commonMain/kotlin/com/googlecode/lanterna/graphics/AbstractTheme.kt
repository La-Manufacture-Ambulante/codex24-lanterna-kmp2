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
import java.util.Arrays
import java.util.EnumSet
import java.util.HashMap
import java.util.LinkedList
import java.util.regex.Pattern

/**
 * Abstract [Theme] implementation that manages a hierarchical tree of theme nodes ties to Class objects.
 * Sub-classes will inherit their theme properties from super-class definitions, the java.lang.Object class is
 * considered the root of the tree and as such is the fallback for all other classes.
 *
 * You normally use this class through [PropertyTheme], which is the default implementation bundled with Lanterna.
 * @author Martin
 */
abstract class AbstractTheme protected constructor(
    final override val windowPostRenderer: WindowPostRenderer?,
    final override val windowDecorationRenderer: WindowDecorationRenderer?
) : Theme {

    private val rootNode: ThemeTreeNode = ThemeTreeNode(Any::class.java, null)

    override val defaultDefinition: ThemeDefinition
        get() = DefinitionImpl(rootNode)

    init {
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

    protected fun addStyle(definition: String?, style: String?, value: String?): Boolean {
        val node = getNode(definition) ?: return false
        node.apply(style, value)
        return true
    }

    private fun getNode(definition: String?): ThemeTreeNode? {
        return try {
            if (definition.isNullOrBlank()) {
                getNode(Any::class.java)
            } else {
                getNode(Class.forName(definition))
            }
        } catch (_: ClassNotFoundException) {
            null
        }
    }

    private fun getNode(definition: Class<*>): ThemeTreeNode {
        if (definition == Any::class.java) {
            return rootNode
        }
        val parent = getNode(definition.superclass ?: Any::class.java)
        val existing = parent.childMap[definition]
        if (existing != null) {
            return existing
        }
        val node = ThemeTreeNode(definition, parent)
        parent.childMap[definition] = node
        return node
    }

    override fun getDefinition(clazz: Class<*>?): ThemeDefinition {
        var currentClass = clazz
        val hierarchy = LinkedList<Class<*>>()
        while (currentClass != null && currentClass != Any::class.java) {
            hierarchy.addFirst(currentClass)
            currentClass = currentClass.superclass
        }

        var node = rootNode
        for (aClass in hierarchy) {
            val child = node.childMap[aClass]
            if (child != null) {
                node = child
            } else {
                break
            }
        }
        return DefinitionImpl(node)
    }

    /**
     * Returns redundant theme declarations. A declaration is redundant if the same value already exists in a parent
     * node and removing the declaration would produce the same effective style.
     */
    fun findRedundantDeclarations(): List<String?>? {
        val result = ArrayList<String?>()
        for (node in rootNode.childMap.values) {
            findRedundantDeclarations(result, node)
        }
        result.sortBy { it ?: "" }
        return result
    }

    private fun findRedundantDeclarations(result: MutableList<String?>, node: ThemeTreeNode) {
        for (style in node.foregroundMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val color = node.foregroundMap[style]
            val colorFromParent = StyleImpl(node.parent, style).foreground
            if (color == colorFromParent) {
                result.add(node.clazz.name + ".foreground" + formattedStyle)
            }
        }

        for (style in node.backgroundMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val color = node.backgroundMap[style]
            val colorFromParent = StyleImpl(node.parent, style).background
            if (color == colorFromParent) {
                result.add(node.clazz.name + ".background" + formattedStyle)
            }
        }

        for (style in node.sgrMap.keys) {
            var formattedStyle = "[$style]"
            if (formattedStyle.length == 2) {
                formattedStyle = ""
            }
            val sgrs = node.sgrMap[style]
            val sgrsFromParent = StyleImpl(node.parent, style).sgRs
            if (sgrs == sgrsFromParent) {
                result.add(node.clazz.name + ".sgr" + formattedStyle)
            }
        }

        for (childNode in node.childMap.values) {
            findRedundantDeclarations(result, childNode)
        }
    }

    private inner class DefinitionImpl(private val node: ThemeTreeNode) : ThemeDefinition {
        override val normal: ThemeStyle
            get() = StyleImpl(node, STYLE_NORMAL)

        override val preLight: ThemeStyle
            get() = StyleImpl(node, STYLE_PRELIGHT)

        override val selected: ThemeStyle
            get() = StyleImpl(node, STYLE_SELECTED)

        override val active: ThemeStyle
            get() = StyleImpl(node, STYLE_ACTIVE)

        override val insensitive: ThemeStyle
            get() = StyleImpl(node, STYLE_INSENSITIVE)

        override val isCursorVisible: Boolean
            get() {
                val cursorVisible = node.cursorVisible
                if (cursorVisible == null) {
                    return if (node === rootNode) {
                        true
                    } else {
                        DefinitionImpl(node.parent!!).isCursorVisible
                    }
                }
                return cursorVisible
            }

        override fun getCustom(name: String?): ThemeStyle {
            return StyleImpl(node, name)
        }

        override fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle? {
            val customStyle = getCustom(name)
            return customStyle ?: defaultValue
        }

        override fun getCharacter(name: String?, fallback: Char): Char {
            val character = node.characterMap[name]
            if (character == null) {
                return if (node === rootNode) {
                    fallback
                } else {
                    DefinitionImpl(node.parent!!).getCharacter(name, fallback)
                }
            }
            return character
        }

        override fun getIntegerProperty(name: String?, defaultValue: Int): Int {
            val propertyValue = node.propertyMap[name]
            if (propertyValue == null) {
                return if (node === rootNode) {
                    defaultValue
                } else {
                    DefinitionImpl(node.parent!!).getIntegerProperty(name, defaultValue)
                }
            }
            return Integer.parseInt(propertyValue)
        }

        override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
            val propertyValue = node.propertyMap[name]
            if (propertyValue == null) {
                return if (node === rootNode) {
                    defaultValue
                } else {
                    DefinitionImpl(node.parent!!).getBooleanProperty(name, defaultValue)
                }
            }
            return java.lang.Boolean.parseBoolean(propertyValue)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Component?> getRenderer(type: Class<T?>?): ComponentRenderer<T?>? {
            val rendererClass = node.renderer
            if (rendererClass == null) {
                return if (node === rootNode) {
                    null
                } else {
                    DefinitionImpl(node.parent!!).getRenderer(type)
                }
            }
            return instanceByClassName(rendererClass) as ComponentRenderer<T?>?
        }
    }

    private inner class StyleImpl(
        private val styleNode: ThemeTreeNode?,
        private val name: String?
    ) : ThemeStyle {

        override val foreground: TextColor
            get() {
                var node = styleNode
                while (node != null) {
                    if (node.foregroundMap.containsKey(name)) {
                        return node.foregroundMap[name]!!
                    }
                    node = node.parent
                }
                return rootNode.foregroundMap[STYLE_NORMAL] ?: TextColor.ANSI.WHITE
            }

        override val background: TextColor
            get() {
                var node = styleNode
                while (node != null) {
                    if (node.backgroundMap.containsKey(name)) {
                        return node.backgroundMap[name]!!
                    }
                    node = node.parent
                }
                return rootNode.backgroundMap[STYLE_NORMAL] ?: TextColor.ANSI.BLACK
            }

        override val sgRs: EnumSet<SGR>
            get() {
                var node = styleNode
                while (node != null) {
                    val sgr = node.sgrMap[name]
                    if (sgr != null) {
                        return EnumSet.copyOf(sgr)
                    }
                    node = node.parent
                }
                val fallback = rootNode.sgrMap[STYLE_NORMAL] ?: EnumSet.noneOf(SGR::class.java)
                return EnumSet.copyOf(fallback)
            }
    }

    private class ThemeTreeNode(
        val clazz: Class<*>,
        val parent: ThemeTreeNode?
    ) {
        val childMap: MutableMap<Class<*>, ThemeTreeNode> = HashMap()
        val foregroundMap: MutableMap<String?, TextColor> = HashMap()
        val backgroundMap: MutableMap<String?, TextColor> = HashMap()
        val sgrMap: MutableMap<String?, EnumSet<SGR>> = HashMap()
        val characterMap: MutableMap<String?, Char> = HashMap()
        val propertyMap: MutableMap<String?, String?> = HashMap()
        var cursorVisible: Boolean? = true
        var renderer: String? = null

        fun apply(style: String?, value: String?) {
            val trimmedValue = value?.trim() ?: ""
            val matcher = STYLE_FORMAT.matcher(style ?: "")
            if (!matcher.matches()) {
                throw IllegalArgumentException("Unknown style declaration: $style")
            }
            val styleComponent = matcher.group(1)
            val group = if (matcher.groupCount() > 2) matcher.group(3) else null
            when (styleComponent.lowercase().trim()) {
                "foreground" -> foregroundMap[getCategory(group)] = parseValue(trimmedValue)
                "background" -> backgroundMap[getCategory(group)] = parseValue(trimmedValue)
                "sgr" -> sgrMap[getCategory(group)] = parseSGR(trimmedValue)
                "char" -> characterMap[getCategory(group)] = if (trimmedValue.isEmpty()) ' ' else trimmedValue[0]
                "cursor" -> cursorVisible = java.lang.Boolean.parseBoolean(trimmedValue)
                "property" -> propertyMap[getCategory(group)] = if (trimmedValue.isEmpty()) null else trimmedValue.trim()
                "renderer" -> renderer = if (trimmedValue.isBlank()) null else trimmedValue.trim()
                "postrenderer", "windowdecoration" -> {
                    // Don't do anything with this now, we might use it later
                }

                else -> throw IllegalArgumentException(
                    "Unknown style component \"$styleComponent\" in style \"$style\""
                )
            }
        }

        private fun parseValue(value: String): TextColor {
            return TextColor.Factory.fromString(value)
                ?: throw IllegalArgumentException("Unable to parse color value \"$value\"")
        }

        private fun parseSGR(value: String): EnumSet<SGR> {
            val sgrEntries = value.trim().split(",")
            val sgrSet = EnumSet.noneOf(SGR::class.java)
            for (rawEntry in sgrEntries) {
                val entry = rawEntry.trim().uppercase()
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
            for (style in Arrays.asList(STYLE_ACTIVE, STYLE_INSENSITIVE, STYLE_PRELIGHT, STYLE_NORMAL, STYLE_SELECTED)) {
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
            if (className.isNullOrBlank()) {
                return null
            }
            return try {
                Class.forName(className).getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}
