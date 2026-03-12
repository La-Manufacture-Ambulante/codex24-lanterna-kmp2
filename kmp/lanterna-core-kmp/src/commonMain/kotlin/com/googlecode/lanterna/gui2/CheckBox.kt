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
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The checkbox component looks like a regular checkbox that you can find in modern graphics user interfaces, a label
 * and a space that the user can toggle on and off by using enter or space keys.
 *
 * @author Martin
 */
class CheckBox(label: String) : AbstractInteractableComponent<CheckBox?>() {
    interface Listener {
        fun onStatusChanged(checked: Boolean)
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()
    private var label: String
    private var checked: Boolean = false

    constructor() : this("")

    init {
        if (label.contains("\n") || label.contains("\r")) {
            throw IllegalArgumentException("Multiline checkbox labels are not supported")
        }
        this.label = label
        this.checked = false
    }

    @Synchronized
    fun setChecked(checked: Boolean): CheckBox {
        this.checked = checked
        runOnGUIThreadIfExistsOtherwiseRunDirect(
            Runnable {
                for (listener in listeners) {
                    listener.onStatusChanged(checked)
                }
            },
        )
        invalidate()
        return this
    }

    fun isChecked(): Boolean {
        return checked
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (isKeyboardActivationStroke(keyStroke)) {
            setChecked(!isChecked())
            return Interactable.Result.HANDLED
        } else if (isMouseActivationStroke(keyStroke)) {
            basePane!!.focusedInteractable = this
            setChecked(!isChecked())
            return Interactable.Result.HANDLED
        }
        return super.handleKeyStroke(keyStroke)
    }

    @Synchronized
    fun setLabel(label: String?): CheckBox {
        if (label == null) {
            throw IllegalArgumentException("Cannot set CheckBox label to null")
        }
        this.label = label
        invalidate()
        return this
    }

    fun getLabel(): String {
        return label
    }

    fun addListener(listener: Listener?): CheckBox {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener)
        }
        return this
    }

    fun removeListener(listener: Listener?): CheckBox {
        listeners.remove(listener)
        return this
    }

    override fun createDefaultRenderer(): CheckBoxRenderer {
        return DefaultCheckBoxRenderer()
    }

    abstract class CheckBoxRenderer : InteractableRenderer<CheckBox?>

    class DefaultCheckBoxRenderer : CheckBoxRenderer() {
        override fun getCursorLocation(component: CheckBox?): TerminalPosition? {
            val c = component ?: return null
            val themeDefinition = c.themeDefinition ?: return null
            return if (themeDefinition.isCursorVisible) CURSOR_LOCATION else null
        }

        override fun getPreferredSize(component: CheckBox?): TerminalSize {
            val c = component ?: return TerminalSize(3, 1)
            var width = 3
            if (c.label.isNotEmpty()) {
                width += 1 + TerminalTextUtils.getColumnWidth(c.label)
            }
            return TerminalSize(width, 1)
        }

        override fun drawComponent(
            graphics: TextGUIGraphics?,
            component: CheckBox?,
        ) {
            val g = graphics ?: return
            val c = component ?: return
            val themeDefinition: ThemeDefinition = c.themeDefinition ?: return

            if (c.isFocused) {
                g.applyThemeStyle(themeDefinition.active)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }

            g.fill(' ')
            g.putString(4, 0, c.label)

            if (c.isFocused) {
                g.applyThemeStyle(themeDefinition.preLight)
            } else {
                g.applyThemeStyle(themeDefinition.insensitive)
            }
            g.setCharacter(0, 0, themeDefinition.getCharacter("LEFT_BRACKET", '['))
            g.setCharacter(2, 0, themeDefinition.getCharacter("RIGHT_BRACKET", ']'))
            g.setCharacter(3, 0, ' ')

            if (c.isFocused) {
                g.applyThemeStyle(themeDefinition.selected)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }
            g.setCharacter(1, 0, if (c.isChecked()) themeDefinition.getCharacter("MARKER", 'x') else ' ')
        }

        companion object {
            private val CURSOR_LOCATION = TerminalPosition(1, 0)
        }
    }
}
