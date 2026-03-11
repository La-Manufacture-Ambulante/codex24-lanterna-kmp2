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
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.input.MouseAction
import com.googlecode.lanterna.input.MouseActionType

/**
 * Default implementation of [Interactable] that extends [AbstractComponent].
 */
abstract class AbstractInteractableComponent<T : AbstractInteractableComponent<T>?> protected constructor() :
    AbstractComponent<T>(),
    Interactable {

    private var inputFilterBacking: InputFilter? = null
    private var inFocus: Boolean = false
    private var enabledBacking: Boolean = true
    private var accelerator: KeyStroke? = null

    override val renderer: InteractableRenderer<T?>?
        get() = super.renderer as InteractableRenderer<T?>?

    override val isFocused: Boolean
        get() = inFocus

    override val isEnabled: Boolean
        get() = enabledBacking

    override open val isFocusable: Boolean
        get() = true

    override val cursorLocation: TerminalPosition?
        get() = renderer?.getCursorLocation(self())

    override val inputFilter: InputFilter?
        get() = inputFilterBacking

    override fun takeFocus(): T? {
        if (!isEnabled) {
            return self()
        }
        basePane?.focusedInteractable = this
        return self()
    }

    override fun onEnterFocus(direction: Interactable.FocusChangeDirection?, previouslyInFocus: Interactable?) {
        inFocus = true
        afterEnterFocus(direction, previouslyInFocus)
    }

    @Suppress("EmptyMethod")
    protected open fun afterEnterFocus(direction: Interactable.FocusChangeDirection?, previouslyInFocus: Interactable?) {
        // By default no action
    }

    @Synchronized
    protected open fun setAccelerator(keyStroke: KeyStroke?): T? {
        accelerator = keyStroke
        return self()
    }

    @Synchronized
    protected open fun getAccelerator(): KeyStroke? {
        return accelerator
    }

    override fun onLeaveFocus(direction: Interactable.FocusChangeDirection?, nextInFocus: Interactable?) {
        inFocus = false
        afterLeaveFocus(direction, nextInFocus)
    }

    @Suppress("EmptyMethod")
    protected open fun afterLeaveFocus(direction: Interactable.FocusChangeDirection?, nextInFocus: Interactable?) {
        // By default no action
    }

    override abstract fun createDefaultRenderer(): InteractableRenderer<T?>?

    @Synchronized
    override fun setEnabled(enabled: Boolean): T? {
        enabledBacking = enabled
        if (!enabled && isFocused) {
            basePane?.focusedInteractable = null
        }
        return self()
    }

    @Synchronized
    override fun handleInput(keyStroke: KeyStroke?): Interactable.Result? {
        if (keyStroke == null) {
            return Interactable.Result.UNHANDLED
        }
        if (inputFilterBacking == null || inputFilterBacking?.onInput(this, keyStroke) == true) {
            return handleKeyStroke(keyStroke)
        }
        return Interactable.Result.UNHANDLED
    }

    protected open fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (!keyStroke.isAltDown && !keyStroke.isCtrlDown && !keyStroke.isShiftDown) {
            return when (keyStroke.keyType) {
                KeyType.ARROW_DOWN -> Interactable.Result.MOVE_FOCUS_DOWN
                KeyType.ARROW_LEFT -> Interactable.Result.MOVE_FOCUS_LEFT
                KeyType.ARROW_RIGHT -> Interactable.Result.MOVE_FOCUS_RIGHT
                KeyType.ARROW_UP -> Interactable.Result.MOVE_FOCUS_UP
                KeyType.TAB -> Interactable.Result.MOVE_FOCUS_NEXT
                KeyType.REVERSE_TAB -> Interactable.Result.MOVE_FOCUS_PREVIOUS
                KeyType.MOUSE_EVENT -> {
                    if (isMouseMove(keyStroke)) {
                        Interactable.Result.UNHANDLED
                    } else {
                        basePane?.focusedInteractable = this
                        Interactable.Result.HANDLED
                    }
                }

                else -> Interactable.Result.UNHANDLED
            }
        }
        return Interactable.Result.UNHANDLED
    }

    @Synchronized
    override fun setInputFilter(inputFilter: InputFilter?): T? {
        inputFilterBacking = inputFilter
        return self()
    }

    fun isKeyboardActivationStroke(keyStroke: KeyStroke): Boolean {
        val isKeyboardActivation =
            (keyStroke.keyType == KeyType.CHARACTER && keyStroke.character == ' ') || keyStroke.keyType == KeyType.ENTER
        return isFocused && isKeyboardActivation
    }

    fun isKeyboardAcceleratorStroke(keyStroke: KeyStroke?): Boolean {
        val currentAccelerator = accelerator ?: return false
        return isEnabled && currentAccelerator == keyStroke
    }

    fun isMouseActivationStroke(keyStroke: KeyStroke?): Boolean {
        val isMouseActivation = if (keyStroke is MouseAction) {
            keyStroke.actionType == MouseActionType.CLICK_DOWN
        } else {
            false
        }
        return isMouseActivation
    }

    fun isActivationStroke(keyStroke: KeyStroke?): Boolean {
        if (keyStroke == null) {
            return false
        }
        val isKeyboardActivationStroke = isKeyboardActivationStroke(keyStroke)
        val isMouseActivationStroke = isMouseActivationStroke(keyStroke)
        return isKeyboardActivationStroke || isMouseActivationStroke
    }

    fun isMouseDown(keyStroke: KeyStroke): Boolean {
        return keyStroke.keyType == KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseDown
    }

    fun isMouseDrag(keyStroke: KeyStroke): Boolean {
        return keyStroke.keyType == KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseDrag
    }

    fun isMouseMove(keyStroke: KeyStroke): Boolean {
        return keyStroke.keyType == KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseMove
    }

    fun isMouseUp(keyStroke: KeyStroke): Boolean {
        return keyStroke.keyType == KeyType.MOUSE_EVENT && (keyStroke as MouseAction).isMouseUp
    }
}
