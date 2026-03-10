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
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.bundle.LanternaThemes
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.graphics.ThemeDefinition

/**
 * AbstractComponent provides some good default behaviour for a [Component], all components in Lanterna extends
 * from this class in some way.
 */
abstract class AbstractComponent<T : Component?> : Component {
    private var overrideRenderer: ComponentRenderer<T?>? = null
    private var themeRenderer: ComponentRenderer<T?>? = null
    private var themeRenderersTheme: Theme? = null
    private var defaultRenderer: ComponentRenderer<T?>? = null

    private var sizeBacking: TerminalSize? = TerminalSize.ZERO
    private var explicitPreferredSize: TerminalSize? = null
    private var positionBacking: TerminalPosition? = TerminalPosition.TOP_LEFT_CORNER
    private var themeOverride: Theme? = null
    private var layoutDataBacking: LayoutData? = null
    private var visibleBacking: Boolean = true

    override var parent: Container? = null

    override open val isInvalid: Boolean
        get() = invalidBacking
    private var invalidBacking: Boolean = true

    override open val renderer: ComponentRenderer<T?>?
        get() {
            if (overrideRenderer != null) {
                return overrideRenderer
            }

            val currentTheme = theme
            if ((themeRenderer == null && basePane != null) ||
                (themeRenderer != null && currentTheme !== themeRenderersTheme)
            ) {
                themeRenderer = currentTheme?.getDefinition(javaClass)?.getRenderer(selfClass())
                if (themeRenderer != null) {
                    themeRenderersTheme = currentTheme
                }
            }
            if (themeRenderer != null) {
                return themeRenderer
            }

            if (defaultRenderer == null) {
                defaultRenderer = createDefaultRenderer()
                if (defaultRenderer == null) {
                    throw IllegalStateException("$javaClass returned a null default renderer")
                }
            }
            return defaultRenderer
        }

    override val preferredSize: TerminalSize?
        get() = explicitPreferredSize ?: calculatePreferredSize()

    override val size: TerminalSize?
        get() = sizeBacking

    override val position: TerminalPosition?
        get() = positionBacking

    override open val globalPosition: TerminalPosition?
        get() = toGlobal(TerminalPosition.TOP_LEFT_CORNER)

    override val layoutData: LayoutData?
        get() = layoutDataBacking

    override val isVisible: Boolean
        get() = visibleBacking

    override open val textGUI: TextGUI?
        get() = parent?.textGUI

    override open val theme: Theme?
        get() {
            if (themeOverride != null) {
                return themeOverride
            }
            if (parent != null) {
                return parent?.theme
            }
            if (basePane != null) {
                return basePane?.theme
            }
            return LanternaThemes.defaultTheme
        }

    override open val themeDefinition: ThemeDefinition?
        get() = theme?.getDefinition(javaClass)

    override open val basePane: BasePane?
        get() = parent?.basePane

    protected abstract fun createDefaultRenderer(): ComponentRenderer<T?>?

    protected fun runOnGUIThreadIfExistsOtherwiseRunDirect(runnable: Runnable?) {
        val guiThread = textGUI?.guiThread
        if (guiThread != null) {
            guiThread.invokeLater { runnable?.run() }
        } else {
            runnable?.run()
        }
    }

    fun setRenderer(renderer: ComponentRenderer<T?>?): T? {
        this.overrideRenderer = renderer
        return self()
    }

    override open fun invalidate() {
        invalidBacking = true
    }

    override open fun setSize(size: TerminalSize?): T? {
        sizeBacking = size
        return self()
    }

    override fun setPreferredSize(explicitPreferredSize: TerminalSize?): T? {
        this.explicitPreferredSize = explicitPreferredSize
        return self()
    }

    override fun setVisible(visible: Boolean): T? {
        if (visibleBacking != visible) {
            visibleBacking = visible
            if (visible) {
                invalidate()
            } else {
                parent?.invalidate()
            }
        }
        return self()
    }

    protected open fun calculatePreferredSize(): TerminalSize? {
        return renderer?.getPreferredSize(self())
    }

    override open fun setPosition(position: TerminalPosition?): T? {
        positionBacking = position
        return self()
    }

    final override fun draw(graphics: TextGUIGraphics?) {
        if (graphics == null) {
            return
        }
        setSize(graphics.size)
        onBeforeDrawing()
        renderer?.drawComponent(graphics, self())
        onAfterDrawing(graphics)
        invalidBacking = false
    }

    protected open fun onBeforeDrawing() {
        // No operation by default
    }

    @Suppress("EmptyMethod")
    protected open fun onAfterDrawing(graphics: TextGUIGraphics?) {
        // No operation by default
    }

    override open fun setLayoutData(data: LayoutData?): T? {
        if (layoutDataBacking !== data) {
            layoutDataBacking = data
            invalidate()
        }
        return self()
    }

    override fun hasParent(parent: Container?): Boolean {
        var recursiveParent = this.parent
        while (recursiveParent != null) {
            if (recursiveParent === parent) {
                return true
            }
            recursiveParent = recursiveParent.parent
        }
        return false
    }

    override open fun setTheme(theme: Theme?): Component? {
        themeOverride = theme
        invalidate()
        return this
    }

    override fun isInside(container: Container?): Boolean {
        var test: Component? = this
        while (test?.parent != null) {
            if (test.parent === container) {
                return true
            }
            test = test.parent
        }
        return false
    }

    override open fun toBasePane(position: TerminalPosition?): TerminalPosition? {
        if (position == null) {
            return null
        }
        val localPosition = this.position ?: return null
        return parent?.toBasePane(localPosition.withRelative(position))
    }

    override open fun toGlobal(position: TerminalPosition?): TerminalPosition? {
        if (position == null) {
            return null
        }
        val localPosition = this.position ?: return null
        return parent?.toGlobal(localPosition.withRelative(position))
    }

    override open fun withBorder(border: Border?): Border? {
        border?.component = this
        return border
    }

    override open fun addTo(panel: Panel?): T? {
        panel?.addComponent(this)
        return self()
    }

    override open fun onAdded(container: Container?) {
        if (parent !== container && parent != null) {
            parent?.removeComponent(this)
        }
        parent = container
    }

    override open fun onRemoved(container: Container?) {
        if (parent === container) {
            parent = null
            themeRenderer = null
        } else {
            throw IllegalStateException("$this is not $container's child.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun self(): T {
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun selfClass(): Class<T?> {
        return javaClass as Class<T?>
    }
}
