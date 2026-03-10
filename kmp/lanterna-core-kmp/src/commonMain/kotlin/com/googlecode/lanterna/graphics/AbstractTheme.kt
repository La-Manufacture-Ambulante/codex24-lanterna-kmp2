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
 * Common-safe theme tree implementation keyed by component class name.
 */
abstract class AbstractTheme protected constructor(
    final override val windowPostRenderer: WindowPostRenderer?,
    final override val windowDecorationRenderer: WindowDecorationRenderer?,
) : Theme {
    private val nodes: MutableMap<String, ThemeTreeNode> = linkedMapOf()
    private val rootNode = ThemeTreeNode(ROOT_KEY)

    override val defaultDefinition: ThemeDefinition
        get() = DefinitionImpl(rootNode)

    init {
        rootNode.foregroundMap[STYLE_NORMAL] = TextColor.ANSI.WHITE
        rootNode.backgroundMap[STYLE_NORMAL] = TextColor.ANSI.BLACK
        classloadStandardRenderersForGraal()
    }

    protected fun addStyle(definition: String?, style: String?, value: String?): Boolean {
        val key = definition?.trim().orEmpty().ifBlank { ROOT_KEY }
        val node = nodes.getOrPut(key) { ThemeTreeNode(key) }
        node.apply(style, value)
        return true
    }

    override fun getDefinition(clazz: KClass<*>?): ThemeDefinition {
        val key = clazz?.qualifiedName?.ifBlank { clazz.simpleName } ?: ROOT_KEY
        val node = nodes[key] ?: rootNode
        return DefinitionImpl(node)
    }

    fun findRedundantDeclarations(): List<String?>? = emptyList()

    private fun classloadStandardRenderersForGraal() {
        // Keep original side-effects for JVM static initialization paths.
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
            get() = node.cursorVisible ?: true

        override fun getCustom(name: String?): ThemeStyle {
            return StyleImpl(node, name)
        }

        override fun getCustom(name: String?, defaultValue: ThemeStyle?): ThemeStyle {
            return StyleImpl(node, name ?: STYLE_NORMAL).takeIf {
                node.foregroundMap.containsKey(name) ||
                    node.backgroundMap.containsKey(name) ||
                    node.sgrMap.containsKey(name)
            } ?: defaultValue ?: normal
        }

        override fun getBooleanProperty(name: String?, defaultValue: Boolean): Boolean {
            return node.propertyMap[name]?.toBooleanStrictOrNull() ?: defaultValue
        }

        override fun getIntegerProperty(name: String?, defaultValue: Int): Int {
            return node.propertyMap[name]?.toIntOrNull() ?: defaultValue
        }

        override fun getCharacter(name: String?, fallback: Char): Char {
            return node.characterMap[name] ?: fallback
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Component> getRenderer(type: KClass<T>?): ComponentRenderer<T?>? {
            val rendererClass = node.renderer ?: return null
            return instanceByClassName(rendererClass) as? ComponentRenderer<T?>
        }
    }

    private inner class StyleImpl(
        private val node: ThemeTreeNode,
        private val name: String?,
    ) : ThemeStyle {
        override val foreground: TextColor
            get() = node.foregroundMap[name]
                ?: node.foregroundMap[STYLE_NORMAL]
                ?: TextColor.ANSI.WHITE

        override val background: TextColor
            get() = node.backgroundMap[name]
                ?: node.backgroundMap[STYLE_NORMAL]
                ?: TextColor.ANSI.BLACK

        override val sgRs: EnumSet<SGR>
            get() = EnumSet.copyOf(node.sgrMap[name] ?: node.sgrMap[STYLE_NORMAL] ?: EnumSet.noneOf(SGR::class))
    }

    private inner class ThemeTreeNode(
        val classKey: String,
    ) {
        val foregroundMap: MutableMap<String?, TextColor> = linkedMapOf()
        val backgroundMap: MutableMap<String?, TextColor> = linkedMapOf()
        val sgrMap: MutableMap<String?, EnumSet<SGR>> = linkedMapOf()
        val propertyMap: MutableMap<String?, String> = linkedMapOf()
        val characterMap: MutableMap<String?, Char> = linkedMapOf()
        var renderer: String? = null
        var cursorVisible: Boolean? = null

        fun apply(style: String?, value: String?) {
            val safeStyle = style?.trim().orEmpty()
            if (safeStyle.isEmpty()) {
                return
            }
            val matcher = STYLE_FORMAT.matcher(safeStyle)
            if (!matcher.matches()) {
                return
            }
            val styleName = matcher.group(1).lowercase()
            val group = if (matcher.groupCount() >= 3) matcher.group(3) else null
            when (styleName) {
                "foreground" -> parseColor(value)?.let { foregroundMap[group ?: STYLE_NORMAL] = it }
                "background" -> parseColor(value)?.let { backgroundMap[group ?: STYLE_NORMAL] = it }
                "sgr" -> sgrMap[group ?: STYLE_NORMAL] = parseSgr(value.orEmpty())
                "renderer" -> renderer = value
                "cursor", "cursorvisible" -> cursorVisible = value?.toBooleanStrictOrNull()
                "character" -> value?.firstOrNull()?.let { characterMap[group] = it }
                "property" -> propertyMap[group] = value.orEmpty()
            }
        }
    }

    companion object {
        private const val ROOT_KEY = ""

        internal const val STYLE_NORMAL = "NORMAL"
        internal const val STYLE_PRELIGHT = "PRELIGHT"
        internal const val STYLE_SELECTED = "SELECTED"
        internal const val STYLE_ACTIVE = "ACTIVE"
        internal const val STYLE_INSENSITIVE = "INSENSITIVE"

        private val STYLE_FORMAT = Pattern.compile("([a-zA-Z]+)(\\[([a-zA-Z0-9-_]+)])?")

        fun instanceByClassName(className: String?): Any? {
            // Common-safe fallback: dynamic reflection loading is not available in all targets.
            return null
        }

        private fun parseColor(value: String?): TextColor? {
            if (value.isNullOrBlank()) {
                return null
            }
            return runCatching { TextColor.Factory.fromString(value) }.getOrNull()
        }

        private fun parseSgr(value: String): EnumSet<SGR> {
            val set = EnumSet.noneOf(SGR::class)
            value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { token ->
                runCatching { SGR.valueOf(token.uppercase()) }.getOrNull()?.let { set.add(it) }
            }
            return set
        }
    }
}
