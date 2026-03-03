package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke
import java.util.Collection
import java.util.Collections

abstract class AbstractComposite<T : Container> : AbstractComponent<T>(), Composite, Container {

    private var component: Component? = null

    override fun setComponent(component: Component?) {
        val oldComponent = this.component
        if (oldComponent === component) {
            return
        }
        if (oldComponent != null) {
            removeComponent(oldComponent)
        }
        if (component != null) {
            this.component = component
            component.onAdded(this)
            if (getBasePane() != null) {
                val menuBar: MenuBar? = getBasePane().menuBar
                if (menuBar == null || menuBar.isEmptyMenuBar()) {
                    component.position = TerminalPosition.TOP_LEFT_CORNER
                } else {
                    component.position = TerminalPosition.TOP_LEFT_CORNER.withRelativeRow(1)
                }
            }
            invalidate()
        }
    }

    override fun getComponent(): Component? {
        return component
    }

    override fun getChildCount(): Int {
        return if (component != null) 1 else 0
    }

    override fun getChildrenList(): List<Component> {
        val c = component
        return if (c != null) {
            Collections.singletonList(c)
        } else {
            Collections.emptyList()
        }
    }

    override fun getChildren(): Collection<Component> {
        return getChildrenList()
    }

    override fun containsComponent(component: Component?): Boolean {
        return component != null && component.hasParent(this)
    }

    override fun removeComponent(component: Component?): Boolean {
        if (this.component === component) {
            this.component = null
            val c = component ?: throw NullPointerException()
            c.onRemoved(this)
            invalidate()
            return true
        }
        return false
    }

    override fun isInvalid(): Boolean {
        val c = component
        return c != null && c.isInvalid()
    }

    override fun invalidate() {
        super.invalidate()

        if (component != null) {
            val c = component
            if (c != null) {
                c.invalidate()
            }
        }
    }

    override fun nextFocus(fromThis: Interactable?): Interactable? {
        if (fromThis == null && getComponent() is Interactable) {
            val interactable = getComponent() as Interactable
            if (interactable.isEnabled()) {
                return interactable
            }
        } else if (getComponent() is Container) {
            return (getComponent() as Container).nextFocus(fromThis)
        }
        return null
    }

    override fun previousFocus(fromThis: Interactable?): Interactable? {
        if (fromThis == null && getComponent() is Interactable) {
            val interactable = getComponent() as Interactable
            if (interactable.isEnabled()) {
                return interactable
            }
        } else if (getComponent() is Container) {
            return (getComponent() as Container).previousFocus(fromThis)
        }
        return null
    }

    override fun handleInput(key: KeyStroke?): Boolean {
        return false
    }

    override fun updateLookupMap(interactableLookupMap: InteractableLookupMap?) {
        if (getComponent() is Container) {
            (getComponent() as Container).updateLookupMap(interactableLookupMap)
        } else if (getComponent() is Interactable) {
            val lookupMap = interactableLookupMap ?: throw NullPointerException()
            lookupMap.add(getComponent() as Interactable)
        }
    }
}
