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
import kotlin.collections.ArrayList
import com.googlecode.lanterna.internal.compat.synchronizedCompat

/**
 * This class is the basic building block for creating user interfaces, being the standard implementation of
 * `Container` that supports multiple children. A `Panel` is a component that can contain one or more
 * other components, including nested panels. The panel itself doesn't have any particular appearance and isn't
 * interactable by itself, although you can set a border for the panel and interactable components inside the panel will
 * receive input focus as expected.
 *
 * @author Martin
 */
open class Panel constructor(layoutManager: LayoutManager? = LinearLayout()) :
    AbstractComponent<Panel?>(),
    Container {
    private val components: MutableList<Component> = ArrayList()
    private var layoutManager: LayoutManager = layoutManager ?: AbsoluteLayout()
    private var cachedPreferredSize: TerminalSize? = null

    /**
     * Returns the color used to override the default background color from the theme, if set. Otherwise `null` is
     * returned and whatever theme is assigned will be used to derive the fill color.
     *
     * Sets an override color to be used instead of the theme's color for Panels when drawing unused space. If called
     * with `null`, it will reset back to the theme's color.
     *
     * @return The color, if any, used to fill the panel's unused space instead of the theme's color
     */
    var fillColorOverride: TextColor? = null

    override val childCount: Int
        get() = synchronizedCompat(components) { components.size }

    override val children: Collection<Component?>
        get() = childrenList

    override val childrenList: List<Component?>
        get() = synchronizedCompat(components) { ArrayList(components) }

    override val isInvalid: Boolean
        get() {
            synchronizedCompat(components) {
                for (component in components) {
                    if (component.isVisible && component.isInvalid) {
                        return true
                    }
                }
            }
            return super.isInvalid || layoutManager.hasChanged()
        }

    /**
     * Adds a new child component to the panel. Where within the panel the child will be displayed is up to the layout
     * manager assigned to this panel. If the component has already been added to another panel, it will first be
     * removed from that panel before added to this one.
     * @param component Child component to add to this panel
     * @return Itself
     */
    fun addComponent(component: Component?): Panel {
        return addComponent(Int.MAX_VALUE, component)
    }

    /**
     * Adds a new child component to the panel. Where within the panel the child will be displayed is up to the layout
     * manager assigned to this panel. If the component has already been added to another panel, it will first be
     * removed from that panel before added to this one.
     * @param component Child component to add to this panel
     * @param index At what index to add the component among the existing components
     * @return Itself
     */
    fun addComponent(index: Int, component: Component?): Panel {
        requireNotNull(component) { "Cannot add null component" }

        var insertionIndex = index
        synchronizedCompat(components) {
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

    /**
     * This method is a shortcut for calling:
     * `component.setLayoutData(layoutData); panel.addComponent(component);`
     * @param component Component to add to the panel
     * @param layoutData Layout data to assign to the component
     * @return Itself
     */
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

        synchronizedCompat(components) {
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

    /**
     * Removes all child components from this panel.
     * @return Itself
     */
    fun removeAllComponents(): Panel {
        synchronizedCompat(components) {
            for (component in ArrayList(components)) {
                removeComponent(component)
            }
        }
        return this
    }

    fun setLayoutManager(layoutManager: LayoutManager?): Panel {
        this.layoutManager = layoutManager ?: AbsoluteLayout()
        invalidate()
        return this
    }

    /**
     * Returns the layout manager assigned to this panel.
     * @return Layout manager assigned to this panel
     */
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

        synchronizedCompat(components) {
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
        val reversedComponents = synchronizedCompat(components) { ArrayList(components) }
        reversedComponents.reverse()

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
        synchronizedCompat(components) {
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
        synchronizedCompat(components) {
            for (component in components) {
                component.invalidate()
            }
        }
    }

    private fun layout(size: TerminalSize?) {
        synchronizedCompat(components) {
            layoutManager.doLayout(size, ArrayList(components))
        }
    }

    inner class DefaultPanelRenderer : ComponentRenderer<Panel?> {
        private var fillAreaBeforeDrawingComponents: Boolean = true

        /**
         * If setting this to `false` (default is `true`), the [Panel] will not reset its drawable
         * area with the space character `' '` before drawing all the components.
         * @param fillAreaBeforeDrawingComponents Should the panel area be cleared before drawing components?
         */
        fun setFillAreaBeforeDrawingComponents(fillAreaBeforeDrawingComponents: Boolean) {
            this.fillAreaBeforeDrawingComponents = fillAreaBeforeDrawingComponents
        }

        override fun getPreferredSize(component: Panel?): TerminalSize? {
            synchronizedCompat(components) {
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

            synchronizedCompat(components) {
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
