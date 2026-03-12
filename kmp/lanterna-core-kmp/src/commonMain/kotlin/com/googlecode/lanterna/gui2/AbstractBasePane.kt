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
 * Copyright (C) 2010-2024 Martin Berglund
 */
package com.googlecode.lanterna.gui2

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.gui2.Interactable.Result
import com.googlecode.lanterna.gui2.menu.MenuBar
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This abstract implementation of [BasePane] has the common code shared by all different concrete implementations.
 */
abstract class AbstractBasePane<T : BasePane?> protected constructor() : BasePane {
    protected val contentHolder: ContentHolder = ContentHolder()
    private val listeners: CopyOnWriteArrayList<BasePaneListener<T>> = CopyOnWriteArrayList()

    protected var interactableLookupMap: InteractableLookupMap = InteractableLookupMap(TerminalSize(80, 25))

    private var focusedInteractableBacking: Interactable? = null
    private var invalid: Boolean = false
    private var strictFocusChange: Boolean = false
    private var enableDirectionBasedMovements: Boolean = true
    private var themeOverride: Theme? = null
    private var mouseDownForDrag: Interactable? = null

    override val isInvalid: Boolean
        get() = invalid || contentHolder.isInvalid

    override var component: Component?
        get() = contentHolder.component
        set(value) {
            contentHolder.component = value
        }

    override var focusedInteractable: Interactable?
        get() = focusedInteractableBacking
        set(value) {
            setFocusedInteractable(
                value,
                if (value != null) Interactable.FocusChangeDirection.TELEPORT else Interactable.FocusChangeDirection.RESET,
            )
        }

    override val cursorPosition: TerminalPosition?
        get() {
            val focused = focusedInteractableBacking ?: return null
            val position = focused.cursorLocation ?: return null
            val focusedSize = focused.size ?: return null
            if (position.column < 0 ||
                position.row < 0 ||
                position.column >= focusedSize.columns ||
                position.row >= focusedSize.rows
            ) {
                return null
            }
            return focused.toBasePane(position)
        }

    override var theme: Theme?
        @Synchronized get() {
            if (themeOverride != null) {
                return themeOverride
            }
            return textGUI?.theme
        }

        @Synchronized set(value) {
            themeOverride = value
            invalidate()
        }

    override var menuBar: MenuBar?
        get() = contentHolder.getMenuBar()
        set(value) {
            contentHolder.setMenuBar(value)
        }

    protected val basePaneListeners: List<BasePaneListener<T>>
        get() = listeners

    override fun invalidate() {
        invalid = true
        contentHolder.invalidate()
    }

    override fun draw(graphics: TextGUIGraphics?) {
        if (graphics == null) {
            return
        }

        graphics.applyThemeStyle(theme?.getDefinition(Window::class.java)?.normal)
        graphics.fill(' ')

        val graphicsSize = graphics.size ?: TerminalSize.ZERO
        if (interactableLookupMap.size != graphicsSize) {
            interactableLookupMap = InteractableLookupMap(graphicsSize)
        } else {
            interactableLookupMap.reset()
        }

        contentHolder.draw(graphics)
        contentHolder.updateLookupMap(interactableLookupMap)
        invalid = false
    }

    override fun handleInput(key: KeyStroke?): Boolean {
        val event = key ?: return false

        val deliverEvent = AtomicBoolean(true)
        for (listener in listeners) {
            listener.onInput(self(), event, deliverEvent)
        }
        if (!deliverEvent.get()) {
            return true
        }

        var handled = doHandleInput(event)
        if (!handled) {
            val hasBeenHandled = AtomicBoolean(false)
            for (listener in listeners) {
                listener.onUnhandledInput(self(), event, hasBeenHandled)
            }
            handled = hasBeenHandled.get()
        }
        return handled
    }

    protected abstract fun self(): T

    private fun doHandleInput(key: KeyStroke): Boolean {
        if (key.keyType == KeyType.MOUSE_EVENT) {
            return handleMouseInput(key as MouseAction)
        }

        var direction = Interactable.FocusChangeDirection.TELEPORT
        var nextFocus: Interactable? = null
        var result = false

        val focused = focusedInteractableBacking
        if (focused == null) {
            val activeMenuBar = menuBar
            val baseComponent = component
            when (key.keyType) {
                KeyType.TAB, KeyType.ARROW_RIGHT, KeyType.ARROW_DOWN -> {
                    direction = Interactable.FocusChangeDirection.NEXT
                    nextFocus = activeMenuBar?.nextFocus(null)
                    if (nextFocus == null) {
                        nextFocus =
                            when (baseComponent) {
                                is Container -> baseComponent.nextFocus(null)
                                is Interactable -> baseComponent
                                else -> null
                            }
                    }
                }

                KeyType.REVERSE_TAB, KeyType.ARROW_UP, KeyType.ARROW_LEFT -> {
                    direction = Interactable.FocusChangeDirection.PREVIOUS
                    nextFocus =
                        when (baseComponent) {
                            is Container -> baseComponent.previousFocus(null)
                            is Interactable -> baseComponent
                            else -> null
                        }
                    if (nextFocus == null) {
                        nextFocus = activeMenuBar?.previousFocus(null)
                    }
                }

                else -> {
                    // no-op
                }
            }
            if (nextFocus != null) {
                setFocusedInteractable(nextFocus, direction)
                result = true
            }
        } else {
            var handleResult = focused.handleInput(key)
            if (!enableDirectionBasedMovements) {
                if (handleResult == Result.MOVE_FOCUS_DOWN || handleResult == Result.MOVE_FOCUS_RIGHT) {
                    handleResult = Result.MOVE_FOCUS_NEXT
                } else if (handleResult == Result.MOVE_FOCUS_UP || handleResult == Result.MOVE_FOCUS_LEFT) {
                    handleResult = Result.MOVE_FOCUS_PREVIOUS
                }
            }

            when (handleResult) {
                Result.HANDLED -> {
                    result = true
                }

                Result.UNHANDLED -> {
                    var parent = focused.parent
                    while (parent != null) {
                        if (parent.handleInput(key)) {
                            return true
                        }
                        parent = parent.parent
                    }
                    result = false
                }

                Result.MOVE_FOCUS_NEXT -> {
                    nextFocus = contentHolder.nextFocus(focused)
                    if (nextFocus == null) {
                        nextFocus = contentHolder.nextFocus(null)
                    }
                    direction = Interactable.FocusChangeDirection.NEXT
                }

                Result.MOVE_FOCUS_PREVIOUS -> {
                    nextFocus = contentHolder.previousFocus(focused)
                    if (nextFocus == null) {
                        nextFocus = contentHolder.previousFocus(null)
                    }
                    direction = Interactable.FocusChangeDirection.PREVIOUS
                }

                Result.MOVE_FOCUS_DOWN -> {
                    nextFocus = interactableLookupMap.findNextDown(focused)
                    direction = Interactable.FocusChangeDirection.DOWN
                    if (nextFocus == null && !strictFocusChange) {
                        nextFocus = contentHolder.nextFocus(focused)
                        direction = Interactable.FocusChangeDirection.NEXT
                    }
                }

                Result.MOVE_FOCUS_LEFT -> {
                    nextFocus = interactableLookupMap.findNextLeft(focused)
                    direction = Interactable.FocusChangeDirection.LEFT
                }

                Result.MOVE_FOCUS_RIGHT -> {
                    nextFocus = interactableLookupMap.findNextRight(focused)
                    direction = Interactable.FocusChangeDirection.RIGHT
                }

                Result.MOVE_FOCUS_UP -> {
                    nextFocus = interactableLookupMap.findNextUp(focused)
                    direction = Interactable.FocusChangeDirection.UP
                    if (nextFocus == null && !strictFocusChange) {
                        nextFocus = contentHolder.previousFocus(focused)
                        direction = Interactable.FocusChangeDirection.PREVIOUS
                    }
                }

                null -> {
                    result = false
                }
            }
        }

        if (nextFocus != null) {
            setFocusedInteractable(nextFocus, direction)
            result = true
        }
        return result
    }

    private fun handleMouseInput(mouseAction: MouseAction): Boolean {
        val localCoordinates = fromGlobal(mouseAction.position) ?: return false
        val interactable = interactableLookupMap.getInteractableAt(localCoordinates)

        if (mouseAction.isMouseDown) {
            mouseDownForDrag = interactable
        }
        val wasMouseDownForDrag = mouseDownForDrag
        if (mouseAction.isMouseUp) {
            mouseDownForDrag = null
        }

        if (mouseAction.isMouseDrag && mouseDownForDrag != null) {
            return mouseDownForDrag?.handleInput(mouseAction) == Result.HANDLED
        }

        val target = interactable ?: return false
        if (mouseAction.isMouseUp) {
            if (wasMouseDownForDrag === target) {
                return target.handleInput(mouseAction) == Result.HANDLED
            }
            return false
        }
        return target.handleInput(mouseAction) == Result.HANDLED
    }

    protected fun setFocusedInteractable(
        toFocus: Interactable?,
        direction: Interactable.FocusChangeDirection,
    ) {
        if (focusedInteractableBacking === toFocus) {
            return
        }
        if (toFocus != null && !toFocus.isEnabled) {
            return
        }
        focusedInteractableBacking?.onLeaveFocus(direction, toFocus)
        val previous = focusedInteractableBacking
        focusedInteractableBacking = toFocus
        toFocus?.onEnterFocus(direction, previous)
        invalidate()
    }

    override fun setStrictFocusChange(strictFocusChange: Boolean) {
        this.strictFocusChange = strictFocusChange
    }

    override fun setEnableDirectionBasedMovements(enableDirectionBasedMovements: Boolean) {
        this.enableDirectionBasedMovements = enableDirectionBasedMovements
    }

    protected fun addBasePaneListener(basePaneListener: BasePaneListener<T>) {
        listeners.addIfAbsent(basePaneListener)
    }

    protected fun removeBasePaneListener(basePaneListener: BasePaneListener<T>) {
        listeners.remove(basePaneListener)
    }

    protected inner class ContentHolder : AbstractComposite<Container?>() {
        private var internalMenuBar: MenuBar = EmptyMenuBar()

        fun setMenuBar(menuBar: MenuBar?) {
            var resolved = menuBar
            if (resolved == null) {
                resolved = EmptyMenuBar()
            }

            if (internalMenuBar !== resolved) {
                resolved.onAdded(this)
                internalMenuBar.onRemoved(this)
                internalMenuBar = resolved
                if (focusedInteractableBacking == null) {
                    focusedInteractable = resolved.nextFocus(null)
                }
                invalidate()
            }
        }

        fun getMenuBar(): MenuBar {
            return internalMenuBar
        }

        override val isInvalid: Boolean
            get() = super.isInvalid || internalMenuBar.isInvalid

        override fun invalidate() {
            super.invalidate()
            internalMenuBar.invalidate()
        }

        override fun updateLookupMap(interactableLookupMap: InteractableLookupMap?) {
            super.updateLookupMap(interactableLookupMap)
            internalMenuBar.updateLookupMap(interactableLookupMap)
        }

        override var component: Component?
            get() = super.component
            set(value) {
                if (super.component === value) {
                    return
                }
                focusedInteractable = null
                super.component = value
                if (focusedInteractableBacking == null && value is Interactable) {
                    focusedInteractable = value
                } else if (focusedInteractableBacking == null && value is Container) {
                    focusedInteractable = value.nextFocus(null)
                }
            }

        override fun removeComponent(component: Component?): Boolean {
            val removed = super.removeComponent(component)
            if (removed) {
                focusedInteractableBacking = null
            }
            return removed
        }

        override val textGUI: TextGUI?
            get() = this@AbstractBasePane.textGUI

        override val basePane: BasePane
            get() = this@AbstractBasePane

        override fun createDefaultRenderer(): ComponentRenderer<Container?> {
            return object : ComponentRenderer<Container?> {
                override fun getPreferredSize(component: Container?): TerminalSize {
                    val subComponent = this@ContentHolder.component ?: return TerminalSize.ZERO
                    return subComponent.preferredSize ?: TerminalSize.ZERO
                }

                override fun drawComponent(
                    graphics: TextGUIGraphics?,
                    component: Container?,
                ) {
                    var activeGraphics = graphics ?: return

                    if (!internalMenuBar.isEmptyMenuBar) {
                        val menuBarHeight = internalMenuBar.preferredSize?.rows ?: 0
                        val topSlice = activeGraphics.size?.withRows(menuBarHeight)
                        val menuGraphics = activeGraphics.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, topSlice)
                        internalMenuBar.draw(menuGraphics)
                        val remainderSize = activeGraphics.size?.withRelativeRows(-menuBarHeight)
                        val offset = TerminalPosition.TOP_LEFT_CORNER.withRelativeRow(menuBarHeight)
                        activeGraphics = activeGraphics.newTextGraphics(offset, remainderSize) ?: return
                    }

                    val subComponent = this@ContentHolder.component ?: return
                    subComponent.draw(activeGraphics)
                }
            }
        }

        override fun toGlobal(position: TerminalPosition?): TerminalPosition? {
            return this@AbstractBasePane.toGlobal(position)
        }

        override fun toBasePane(position: TerminalPosition?): TerminalPosition? {
            return position
        }
    }

    private class EmptyMenuBar : MenuBar() {
        override val isInvalid: Boolean
            get() = false

        override fun onAdded(container: Container?) {
            // no-op
        }

        override fun onRemoved(container: Container?) {
            // no-op
        }

        override val isEmptyMenuBar: Boolean
            get() = true
    }
}
