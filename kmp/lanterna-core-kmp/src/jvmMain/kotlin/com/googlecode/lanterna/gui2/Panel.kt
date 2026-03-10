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

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.input.KeyStroke
import java.util.ArrayList
import java.util.Collections

/**
 * Standard multi-child [Container] implementation.
 */
open class Panel @JvmOverloads constructor(layoutManager: LayoutManager? = LinearLayout()) :
    AbstractComponent<Panel?>(),
    Container {
    private val components: MutableList<Component> = ArrayList()
    private var layoutManager: LayoutManager = layoutManager ?: AbsoluteLayout()
    private var cachedPreferredSize: TerminalSize? = null

    var fillColorOverride: TextColor? = null

    override val childCount: Int
        get() = synchronized(components) { components.size }

    override val children: Collection<Component?>
        get() = childrenList

    override val childrenList: List<Component?>
        get() = synchronized(components) { ArrayList(components) }

    override val isInvalid: Boolean
        get() {
            synchronized(components) {
                for (component in components) {
                    if (component.isVisible && component.isInvalid) {
                        return true
                    }
                }
            }
            return super.isInvalid || layoutManager.hasChanged()
        }

    fun addComponent(component: Component?): Panel {
        return addComponent(Int.MAX_VALUE, component)
    }

    fun addComponent(index: Int, component: Component?): Panel {
        requireNotNull(component) { "Cannot add null component" }

        var insertionIndex = index
        synchronized(components) {
            if (components.contains(component)) {
                return this
            }
            component.parent?.removeComponent(component)
            if (insertionIndex > components.size) {
                insertionIndex = components.size
            } else if (insertionIndex < 0) {
                insertionIndex = 0
            }
            components.add(insertionIndex, component)
        }
        component.onAdded(this)
        invalidate()
        return this
    }

    fun addComponent(component: Component?, layoutData: LayoutData?): Panel {
        if (component != null) {
            component.setLayoutData(layoutData)
            addComponent(component)
        }
        return this
    }

    override fun containsComponent(component: Component?): Boolean {
        return component != null && component.hasParent(this)
    }

    override fun removeComponent(component: Component?): Boolean {
        requireNotNull(component) { "Cannot remove null component" }

        synchronized(components) {
            val index = components.indexOf(component)
            if (index == -1) {
                return false
            }
            if (basePane?.focusedInteractable === component) {
                basePane?.focusedInteractable = null
            }
            components.removeAt(index)
        }
        component.onRemoved(this)
        invalidate()
        return true
    }

    fun removeAllComponents(): Panel {
        synchronized(components) {
            for (component in ArrayList(components)) {
                removeComponent(component)
            }
        }
        return this
    }

    @Synchronized
    fun setLayoutManager(layoutManager: LayoutManager?): Panel {
        this.layoutManager = layoutManager ?: AbsoluteLayout()
        invalidate()
        return this
    }

    fun getLayoutManager(): LayoutManager {
        return layoutManager
    }

    override fun createDefaultRenderer(): ComponentRenderer<Panel?> {
        return DefaultPanelRenderer()
    }

    override fun calculatePreferredSize(): TerminalSize? {
        if (cachedPreferredSize != null && !isInvalid) {
            return cachedPreferredSize
        }
        return super.calculatePreferredSize()
    }

    override fun nextFocus(fromThis: Interactable?): Interactable? {
        var chooseNextAvailable = fromThis == null

        synchronized(components) {
            for (component in components) {
                if (!component.isVisible) {
                    continue
                }
                if (chooseNextAvailable) {
                    if (component is Interactable && component.isEnabled && component.isFocusable) {
                        return component
                    } else if (component is Container) {
                        val firstInteractable = component.nextFocus(null)
                        if (firstInteractable != null) {
                            return firstInteractable
                        }
                    }
                    continue
                }

                if (component === fromThis) {
                    chooseNextAvailable = true
                    continue
                }

                if (component is Container && fromThis?.isInside(component) == true) {
                    val next = component.nextFocus(fromThis)
                    if (next == null) {
                        chooseNextAvailable = true
                    } else {
                        return next
                    }
                }
            }
        }
        return null
    }

    override fun previousFocus(fromThis: Interactable?): Interactable? {
        var chooseNextAvailable = fromThis == null
        val reversedComponents = synchronized(components) { ArrayList(components) }
        Collections.reverse(reversedComponents)

        for (component in reversedComponents) {
            if (!component.isVisible) {
                continue
            }
            if (chooseNextAvailable) {
                if (component is Interactable && component.isEnabled && component.isFocusable) {
                    return component
                }
                if (component is Container) {
                    val lastInteractable = component.previousFocus(null)
                    if (lastInteractable != null) {
                        return lastInteractable
                    }
                }
                continue
            }

            if (component === fromThis) {
                chooseNextAvailable = true
                continue
            }

            if (component is Container && fromThis?.isInside(component) == true) {
                val next = component.previousFocus(fromThis)
                if (next == null) {
                    chooseNextAvailable = true
                } else {
                    return next
                }
            }
        }
        return null
    }

    override fun handleInput(key: KeyStroke?): Boolean = false

    override fun updateLookupMap(interactableLookupMap: InteractableLookupMap?) {
        synchronized(components) {
            for (component in components) {
                if (!component.isVisible) {
                    continue
                }
                if (component is Container) {
                    component.updateLookupMap(interactableLookupMap)
                } else if (component is Interactable && component.isEnabled && component.isFocusable) {
                    interactableLookupMap?.add(component)
                }
            }
        }
    }

    override fun invalidate() {
        super.invalidate()
        synchronized(components) {
            for (component in components) {
                component.invalidate()
            }
        }
    }

    private fun layout(size: TerminalSize?) {
        synchronized(components) {
            layoutManager.doLayout(size, ArrayList(components))
        }
    }

    inner class DefaultPanelRenderer : ComponentRenderer<Panel?> {
        private var fillAreaBeforeDrawingComponents: Boolean = true

        fun setFillAreaBeforeDrawingComponents(fillAreaBeforeDrawingComponents: Boolean) {
            this.fillAreaBeforeDrawingComponents = fillAreaBeforeDrawingComponents
        }

        override fun getPreferredSize(component: Panel?): TerminalSize? {
            synchronized(components) {
                cachedPreferredSize = layoutManager.getPreferredSize(ArrayList(components))
            }
            return cachedPreferredSize
        }

        override fun drawComponent(graphics: TextGUIGraphics?, panel: Panel?) {
            val targetGraphics = graphics ?: return
            if (isInvalid) {
                layout(targetGraphics.size)
            }

            if (fillAreaBeforeDrawingComponents) {
                targetGraphics.applyThemeStyle(themeDefinition?.normal)
                fillColorOverride?.let { targetGraphics.setBackgroundColor(it) }
                targetGraphics.fill(' ')
            }

            synchronized(components) {
                for (child in components) {
                    if (!child.isVisible) {
                        continue
                    }
                    val componentGraphics = targetGraphics.newTextGraphics(child.position, child.size)
                    child.draw(componentGraphics)
                }
            }
        }
    }
}
