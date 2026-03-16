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
import com.googlecode.lanterna.internal.compat.EnumSet
import com.googlecode.lanterna.internal.compat.Pattern
import kotlin.reflect.KClass

/**
 * Abstract [Theme] implementation that manages a hierarchical tree of theme nodes tied to class definitions.
 *
 * Sub-classes inherit style properties from parent definitions and eventually from the root definition.
 * You normally use this class through [PropertyTheme], which is the default implementation bundled with Lanterna.
 *
 * @author Martin
 */
abstract class AbstractTheme protected constructor(
    final override val windowPostRenderer: WindowPostRenderer?,
    final override val windowDecorationRenderer: WindowDecorationRenderer?,
) : Theme {
    private val rootNode: ThemeTreeNode = ThemeTreeNode(ROOT_CLASS_KEY, null)

    override val defaultDefinition: ThemeDefinition
        get() = DefinitionImpl(rootNode)

    init {
        rootNode.foregroundMap[STYLE_NORMAL] = TextColor.ANSI.WHITE
        rootNode.backgroundMap[STYLE_NORMAL] = TextColor.ANSI.BLACK
        classloadStandardRenderersForGraal()
    }

    protected fun addStyle(
        definition: String?,
        style: String?,
        value: String?,
    ): Boolean {
        val className = definition?.trim().orEmpty()
        val hierarchy =
            if (className.isEmpty()) {
                emptyList()
            } else {
                themeClassHierarchyByName(className) ?: return false
            }

        val node = ensureNodeHierarchy(hierarchy)
        node.apply(style, value)
        return true
    }

    override fun getDefinition(clazz: KClass<*>?): ThemeDefinition {
        if (clazz == null) {
            return DefinitionImpl(rootNode)
        }
        val hierarchy = themeClassHierarchyByType(clazz)
        var node = rootNode
        for (className in hierarchy) {
            node = node.childMap[className] ?: break
        }
        return DefinitionImpl(node)
    }

    /**
     * Returns a list of redundant theme entries in this theme. A redundant entry means it does not need to be
     * specified because a parent node in the hierarchy already has the same property.
     *
     * @return List of redundant theme entries
     */
    fun findRedundantDeclarations(): List<String> {
        val result = arrayListOf<String>()
        for (node in rootNode.childMap.values) {
            findRedundantDeclarations(result, node)
        }
        result.sort()
        return result
    }

    private fun findRedundantDeclarations(
        result: MutableList<String>,
        node: ThemeTreeNode,
    ) {
        val parent = node.parent ?: return
        for (style in node.foregroundMap.keys) {
            val color = node.foregroundMap[style] ?: continue
            val colorFromParent = StyleImpl(parent, style).foreground
            if (color == colorFromParent) {
                result.add("${node.classKey}.foreground${formatStyle(style)}")
            }
        }
        for (style in node.backgroundMap.keys) {
            val color = node.backgroundMap[style] ?: continue
            val colorFromParent = StyleImpl(parent, style).background
            if (color == colorFromParent) {
                result.add("${node.classKey}.background${formatStyle(style)}")
            }
        }
        for (style in node.sgrMap.keys) {
            val sgrs = node.sgrMap[style] ?: continue
            val sgrsFromParent = StyleImpl(parent, style).sgRs
            if (sgrs == sgrsFromParent) {
                result.add("${node.classKey}.sgr${formatStyle(style)}")
            }
        }
        for (child in node.childMap.values) {
            findRedundantDeclarations(result, child)
        }
    }

    private fun ensureNodeHierarchy(hierarchy: List<String>): ThemeTreeNode {
        var node = rootNode
        for (className in hierarchy) {
            node = node.childMap.getOrPut(className) { ThemeTreeNode(className, node) }
        }
        return node
    }

    private fun classloadStandardRenderersForGraal() {
        // This keeps graal static initialization aligned with the Java implementation.
        WindowShadowRenderer::class.toString()
        Button.DefaultButtonRenderer::class.toString()
        Button.FlatButtonRenderer::class.toString()
        Button.BorderedButtonRenderer::class.toString()
    }

    private inner class DefinitionImpl(
        private val node: ThemeTreeNode,
    ) : ThemeDefinition {
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
                var current: ThemeTreeNode? = node
                while (current != null) {
                    return current.cursorVisible
                }
                return true
            }

        override fun getCustom(name: String?): ThemeStyle {
            return StyleImpl(node, name)
        }

        override fun getCustom(
            name: String?,
            defaultValue: ThemeStyle?,
        ): ThemeStyle? {
            var customStyle: ThemeStyle? = getCustom(name)
            if (customStyle == null) {
                customStyle = defaultValue
            }
            return customStyle
        }

        override fun getBooleanProperty(
            name: String?,
            defaultValue: Boolean,
        ): Boolean {
            var current: ThemeTreeNode? = node
            while (current != null) {
                val propertyValue = current.propertyMap[name]
                if (propertyValue != null) {
                    return propertyValue.toBoolean()
                }
                current = current.parent
            }
            return defaultValue
        }

        override fun getIntegerProperty(
            name: String?,
            defaultValue: Int,
        ): Int {
            var current: ThemeTreeNode? = node
            while (current != null) {
                val propertyValue = current.propertyMap[name]
                if (propertyValue != null) {
                    return propertyValue.toInt()
                }
                current = current.parent
            }
            return defaultValue
        }

        override fun getCharacter(
            name: String?,
            fallback: Char,
        ): Char {
            var current: ThemeTreeNode? = node
            while (current != null) {
                val value = current.characterMap[name]
                if (value != null) {
                    return value
                }
                current = current.parent
            }
            return fallback
        }

        override fun <T : Component> getRenderer(type: KClass<T>?): ComponentRenderer<T?>? {
            var current: ThemeTreeNode? = node
            while (current != null) {
                val rendererClass = current.renderer
                if (rendererClass != null) {
                    val rendererInstance = instanceByClassName(rendererClass)
                    if (rendererInstance is ComponentRenderer<*>) {
                        return rendererInstance as? ComponentRenderer<T?>
                    }
                    return null
                }
                current = current.parent
            }
            return null
        }
    }

    private inner class StyleImpl(
        private val styleNode: ThemeTreeNode,
        private val name: String?,
    ) : ThemeStyle {
        override val foreground: TextColor
            get() {
                var node: ThemeTreeNode? = styleNode
                while (node != null) {
                    val color = node.foregroundMap[name]
                    if (color != null) {
                        return color
                    }
                    node = node.parent
                }
                return rootNode.foregroundMap[STYLE_NORMAL] ?: TextColor.ANSI.WHITE
            }

        override val background: TextColor
            get() {
                var node: ThemeTreeNode? = styleNode
                while (node != null) {
                    val color = node.backgroundMap[name]
                    if (color != null) {
                        return color
                    }
                    node = node.parent
                }
                return rootNode.backgroundMap[STYLE_NORMAL] ?: TextColor.ANSI.BLACK
            }

        override val sgRs: EnumSet<SGR>
            get() {
                var node: ThemeTreeNode? = styleNode
                while (node != null) {
                    val sgrs = node.sgrMap[name]
                    if (sgrs != null) {
                        return EnumSet.copyOf(sgrs)
                    }
                    node = node.parent
                }
                val fallback = rootNode.sgrMap[STYLE_NORMAL] ?: EnumSet.noneOf(SGR::class)
                return EnumSet.copyOf(fallback)
            }
    }

    private inner class ThemeTreeNode(
        val classKey: String,
        var parent: ThemeTreeNode?,
    ) {
        val childMap: MutableMap<String, ThemeTreeNode> = linkedMapOf()
        val foregroundMap: MutableMap<String, TextColor> = linkedMapOf()
        val backgroundMap: MutableMap<String, TextColor> = linkedMapOf()
        val sgrMap: MutableMap<String, EnumSet<SGR>> = linkedMapOf()
        val characterMap: MutableMap<String?, Char> = linkedMapOf()
        val propertyMap: MutableMap<String?, String?> = linkedMapOf()
        var cursorVisible: Boolean = true
        var renderer: String? = null

        fun apply(
            style: String?,
            value: String?,
        ) {
            val safeStyle = style?.trim().orEmpty()
            var safeValue = value?.trim().orEmpty()
            val matcher = STYLE_FORMAT.matcher(safeStyle)
            if (!matcher.matches()) {
                throw IllegalArgumentException("Unknown style declaration: $safeStyle")
            }

            val styleComponent = matcher.group(1)
            val group = runCatching { matcher.group(3) }.getOrNull()
            when (styleComponent.lowercase().trim()) {
                "foreground" -> foregroundMap[getCategory(group)] = parseColor(safeValue)
                "background" -> backgroundMap[getCategory(group)] = parseColor(safeValue)
                "sgr" -> sgrMap[getCategory(group)] = parseSgr(safeValue)
                "char" -> characterMap[getCategory(group)] = safeValue.firstOrNull() ?: ' '
                "cursor" -> cursorVisible = safeValue.toBoolean()
                "property" -> propertyMap[getCategory(group)] = safeValue.ifEmpty { null }?.trim()
                "renderer" -> renderer = safeValue.ifEmpty { null }?.trim()
                "postrenderer", "windowdecoration" -> {
                    // Reserved for future use; same behavior as Java implementation.
                }
                else -> throw IllegalArgumentException(
                    "Unknown style component \"$styleComponent\" in style \"$safeStyle\"",
                )
            }
        }
    }

    companion object {
        private const val ROOT_CLASS_KEY = "kotlin.Any"

        internal const val STYLE_NORMAL = ""
        internal const val STYLE_PRELIGHT = "PRELIGHT"
        internal const val STYLE_SELECTED = "SELECTED"
        internal const val STYLE_ACTIVE = "ACTIVE"
        internal const val STYLE_INSENSITIVE = "INSENSITIVE"

        private val STYLE_FORMAT = Pattern.compile("([a-zA-Z]+)(\\[([a-zA-Z0-9-_]+)])?")

        fun instanceByClassName(className: String?): Any? {
            if (className.isNullOrBlank()) {
                return null
            }
            return instantiateThemeClassByName(className)
        }

        private fun formatStyle(style: String): String {
            if (style.isEmpty()) {
                return ""
            }
            return "[$style]"
        }

        private fun parseColor(value: String): TextColor {
            return TextColor.Factory.fromString(value)
        }

        private fun parseSgr(value: String): EnumSet<SGR> {
            val tokens = value.split(',')
            val sgrSet = EnumSet.noneOf(SGR::class)
            for (entry in tokens) {
                val normalized = entry.trim().uppercase()
                if (normalized.isEmpty()) {
                    continue
                }
                try {
                    sgrSet.add(SGR.valueOf(normalized))
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Unknown SGR code \"$normalized\"", e)
                }
            }
            return sgrSet
        }

        private fun getCategory(group: String?): String {
            if (group == null) {
                return STYLE_NORMAL
            }
            val normalized = group.uppercase()
            for (style in listOf(STYLE_ACTIVE, STYLE_INSENSITIVE, STYLE_PRELIGHT, STYLE_NORMAL, STYLE_SELECTED)) {
                if (normalized == style) {
                    return style
                }
            }
            return group
        }
    }
}
