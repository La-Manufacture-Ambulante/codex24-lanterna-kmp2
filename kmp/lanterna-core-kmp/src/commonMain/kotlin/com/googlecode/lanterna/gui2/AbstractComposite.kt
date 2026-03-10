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
import com.googlecode.lanterna.gui2.menu.MenuBar

/**
 * Abstract implementation with common code for [Composite]s.
 */
abstract class AbstractComposite<T : Container?> : AbstractComponent<T>(), Composite, Container {
    private var childComponent: Component? = null

    override open var component: Component?
        get() = childComponent
        set(value) {
            val oldComponent = childComponent
            if (oldComponent === value) {
                return
            }
            if (oldComponent != null) {
                removeComponent(oldComponent)
            }
            if (value != null) {
                childComponent = value
                value.onAdded(this)
                if (basePane != null) {
                    val menuBar: MenuBar? = basePane?.menuBar
                    if (menuBar == null || menuBar.isEmptyMenuBar) {
                        value.setPosition(TerminalPosition.TOP_LEFT_CORNER)
                    } else {
                        value.setPosition(TerminalPosition.TOP_LEFT_CORNER.withRelativeRow(1))
                    }
                }
                invalidate()
            }
        }

    override val childCount: Int
        get() = if (childComponent != null) 1 else 0

    override val childrenList: List<Component?>
        get() = if (childComponent != null) listOf(childComponent) else emptyList()

    override val children: Collection<Component?>
        get() = childrenList

    override open val isInvalid: Boolean
        get() = childComponent != null && childComponent?.isInvalid == true

    override fun containsComponent(component: Component?): Boolean {
        return component != null && component.hasParent(this)
    }

    override open fun removeComponent(component: Component?): Boolean {
        if (childComponent === component) {
            childComponent = null
            component?.onRemoved(this)
            invalidate()
            return true
        }
        return false
    }

    override open fun invalidate() {
        super.invalidate()
        childComponent?.invalidate()
    }

    override open fun nextFocus(fromThis: Interactable?): Interactable? {
        val current = component
        if (fromThis == null && current is Interactable) {
            return if (current.isEnabled) current else null
        }
        if (current is Container) {
            return current.nextFocus(fromThis)
        }
        return null
    }

    override open fun previousFocus(fromThis: Interactable?): Interactable? {
        val current = component
        if (fromThis == null && current is Interactable) {
            return if (current.isEnabled) current else null
        }
        if (current is Container) {
            return current.previousFocus(fromThis)
        }
        return null
    }

    override open fun handleInput(key: com.googlecode.lanterna.input.KeyStroke?): Boolean {
        return false
    }

    override open fun updateLookupMap(interactableLookupMap: InteractableLookupMap?) {
        val current = component
        if (current is Container) {
            current.updateLookupMap(interactableLookupMap)
        } else if (current is Interactable) {
            interactableLookupMap?.add(current)
        }
    }
}
