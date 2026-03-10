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

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.graphics.ThemeDefinition
import com.googlecode.lanterna.input.KeyStroke
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Simple labeled button that the user can trigger by pressing the Enter or the Spacebar key on the keyboard when the
 * component is in focus. You can specify an initial action through one of the constructors and you can also add
 * additional actions to the button using [addListener]. To remove a previously attached action, use
 * [removeListener].
 */
class Button(label: String) : AbstractInteractableComponent<Button>() {
    /**
     * Listener interface that can be used to catch user events on the button
     */
    interface Listener {
        /**
         * This is called when the user has triggered the button
         * @param button Button which was triggered
         */
        fun onTriggered(button: Button)
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()
    private var label: String = " "

    constructor(label: String, action: Runnable) : this(label) {
        listeners.add(object : Listener {
            override fun onTriggered(button: Button) {
                action.run()
            }
        })
    }

    init {
        setLabel(label)
    }

    override fun createDefaultRenderer(): ButtonRenderer {
        return DefaultButtonRenderer()
    }

    override val cursorLocation: TerminalPosition?
        @Synchronized
        get() = renderer?.getCursorLocation(this)

    @Synchronized
    override fun handleKeyStroke(keyStroke: KeyStroke): Interactable.Result? {
        if (isActivationStroke(keyStroke)) {
            triggerActions()
            return Interactable.Result.HANDLED
        }
        return super.handleKeyStroke(keyStroke)
    }

    @Synchronized
    protected fun triggerActions() {
        for (listener in listeners) {
            listener.onTriggered(this)
        }
    }

    @Synchronized
    fun setLabel(label: String) {
        this.label = if (label.isEmpty()) " " else label
        invalidate()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener): Boolean {
        return listeners.remove(listener)
    }

    fun getLabel(): String {
        return label
    }

    override fun toString(): String {
        return "Button{$label}"
    }

    /**
     * Helper interface that doesn't add any new methods but makes coding new button renderers a little bit more clear
     */
    interface ButtonRenderer : InteractableRenderer<Button>

    class DefaultButtonRenderer : ButtonRenderer {
        override fun getCursorLocation(button: Button?): TerminalPosition? {
            val b = button ?: return null
            val themeDefinition = b.themeDefinition ?: return null
            if (themeDefinition.isCursorVisible) {
                return TerminalPosition(1 + getLabelShift(b, b.size ?: TerminalSize.ZERO), 0)
            }
            return null
        }

        override fun getPreferredSize(button: Button?): TerminalSize {
            val b = button ?: return TerminalSize(8, 1)
            return TerminalSize(kotlin.math.max(8, TerminalTextUtils.getColumnWidth(b.getLabel()) + 2), 1)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, button: Button?) {
            val g = graphics ?: return
            val b = button ?: return
            val themeDefinition: ThemeDefinition = b.themeDefinition ?: return

            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.active)
            } else {
                g.applyThemeStyle(themeDefinition.insensitive)
            }
            g.fill(' ')
            g.setCharacter(0, 0, themeDefinition.getCharacter("LEFT_BORDER", '<'))
            g.setCharacter((g.size ?: TerminalSize.ZERO).columns - 1, 0, themeDefinition.getCharacter("RIGHT_BORDER", '>'))

            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.active)
            } else {
                g.applyThemeStyle(themeDefinition.preLight)
            }
            val labelShift = getLabelShift(b, g.size ?: TerminalSize.ZERO)
            g.setCharacter(1 + labelShift, 0, b.getLabel()[0])

            if (TerminalTextUtils.getColumnWidth(b.getLabel()) == 1) {
                return
            }
            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.selected)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }
            g.putString(1 + labelShift + 1, 0, b.getLabel().substring(1))
        }

        private fun getLabelShift(button: Button, size: TerminalSize): Int {
            val availableSpace = size.columns - 2
            if (availableSpace <= 0) {
                return 0
            }
            var labelShift = 0
            val widthInColumns = TerminalTextUtils.getColumnWidth(button.getLabel())
            if (availableSpace > widthInColumns) {
                labelShift = (size.columns - 2 - widthInColumns) / 2
            }
            return labelShift
        }
    }

    class FlatButtonRenderer : ButtonRenderer {
        override fun getCursorLocation(component: Button?): TerminalPosition? {
            return null
        }

        override fun getPreferredSize(component: Button?): TerminalSize {
            val c = component ?: return TerminalSize.ZERO
            return TerminalSize(TerminalTextUtils.getColumnWidth(c.getLabel()), 1)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, button: Button?) {
            val g = graphics ?: return
            val b = button ?: return
            val themeDefinition = b.themeDefinition ?: return

            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.active)
            } else {
                g.applyThemeStyle(themeDefinition.insensitive)
            }
            g.fill(' ')
            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.selected)
            } else {
                g.applyThemeStyle(themeDefinition.normal)
            }
            g.putString(0, 0, b.getLabel())
        }
    }

    class BorderedButtonRenderer : ButtonRenderer {
        override fun getCursorLocation(component: Button?): TerminalPosition? {
            return null
        }

        override fun getPreferredSize(component: Button?): TerminalSize {
            val c = component ?: return TerminalSize.ZERO
            return TerminalSize(TerminalTextUtils.getColumnWidth(c.getLabel()) + 5, 4)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, button: Button?) {
            val g = graphics ?: return
            val b = button ?: return
            val themeDefinition = b.themeDefinition ?: return
            g.applyThemeStyle(themeDefinition.normal)
            val size = g.size ?: TerminalSize.ZERO
            g.drawLine(1, 0, size.columns - 3, 0, Symbols.SINGLE_LINE_HORIZONTAL)
            g.drawLine(1, size.rows - 2, size.columns - 3, size.rows - 2, Symbols.SINGLE_LINE_HORIZONTAL)
            g.drawLine(0, 1, 0, size.rows - 3, Symbols.SINGLE_LINE_VERTICAL)
            g.drawLine(size.columns - 2, 1, size.columns - 2, size.rows - 3, Symbols.SINGLE_LINE_VERTICAL)
            g.setCharacter(0, 0, Symbols.SINGLE_LINE_TOP_LEFT_CORNER)
            g.setCharacter(size.columns - 2, 0, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER)
            g.setCharacter(size.columns - 2, size.rows - 2, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER)
            g.setCharacter(0, size.rows - 2, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER)

            g.drawLine(1, 1, size.columns - 3, 1, ' ')

            if (b.isFocused) {
                g.applyThemeStyle(themeDefinition.active)
            }
            g.putString(2, 1, TerminalTextUtils.fitString(b.getLabel(), size.columns - 5))

            g.applyThemeStyle(themeDefinition.insensitive)
            g.drawLine(1, size.rows - 1, size.columns - 1, size.rows - 1, ' ')
            g.drawLine(size.columns - 1, 1, size.columns - 1, size.rows - 2, ' ')
        }
    }
}
