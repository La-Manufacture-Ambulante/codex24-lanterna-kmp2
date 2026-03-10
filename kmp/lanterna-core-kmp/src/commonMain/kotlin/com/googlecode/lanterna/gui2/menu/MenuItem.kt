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
package com.googlecode.lanterna.gui2.menu

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TerminalTextUtils
import com.googlecode.lanterna.gui2.AbstractInteractableComponent
import com.googlecode.lanterna.gui2.BasePane
import com.googlecode.lanterna.gui2.InteractableRenderer
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.input.KeyStroke

/**
 * This class is a single item that appears in a [Menu] with an optional action attached to it.
 */
open class MenuItem constructor(
    label: String?,
    private val action: Runnable = Runnable {},
) : AbstractInteractableComponent<MenuItem?>() {
    val label: String

    init {
        require(!label.isNullOrBlank()) { "Menu label is not allowed to be null or empty" }
        this.label = label.trim()
    }

    public override fun setAccelerator(keyStroke: KeyStroke?): MenuItem? {
        return super.setAccelerator(keyStroke)
    }

    public override fun getAccelerator(): KeyStroke? {
        return super.getAccelerator()
    }

    override fun createDefaultRenderer(): InteractableRenderer<MenuItem?> {
        return DefaultMenuItemRenderer()
    }

    protected open fun onActivated(): Boolean {
        action.run()
        return true
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): com.googlecode.lanterna.gui2.Interactable.Result? {
        if (isActivationStroke(keyStroke) || isKeyboardAcceleratorStroke(keyStroke)) {
            takeFocus()
            if (onActivated()) {
                val activeBasePane: BasePane? = basePane
                if (activeBasePane is Window && activeBasePane.hints.orEmpty().contains(Window.Hint.MENU_POPUP)) {
                    activeBasePane.close()
                }
            }
            return com.googlecode.lanterna.gui2.Interactable.Result.HANDLED
        } else if (isMouseMove(keyStroke)) {
            takeFocus()
            return com.googlecode.lanterna.gui2.Interactable.Result.HANDLED
        }
        return super.handleKeyStroke(keyStroke)
    }

    abstract class MenuItemRenderer : InteractableRenderer<MenuItem?>

    class DefaultMenuItemRenderer : MenuItemRenderer() {
        override fun getCursorLocation(component: MenuItem?): TerminalPosition? {
            return null
        }

        override fun getPreferredSize(component: MenuItem?): TerminalSize {
            val activeComponent = component ?: return TerminalSize.ONE
            var preferredWidth = TerminalTextUtils.getColumnWidth(activeComponent.label) + 2
            if (activeComponent is Menu && activeComponent.parent !is MenuBar) {
                preferredWidth += 2
            }
            return TerminalSize(preferredWidth, 1)
        }

        override fun drawComponent(graphics: TextGUIGraphics?, menuItem: MenuItem?) {
            val activeGraphics = graphics ?: return
            val activeMenuItem = menuItem ?: return
            val themeDefinition = activeMenuItem.themeDefinition ?: return

            if (activeMenuItem.isFocused) {
                activeGraphics.applyThemeStyle(themeDefinition.selected)
            } else {
                activeGraphics.applyThemeStyle(themeDefinition.normal)
            }

            val activeLabel = activeMenuItem.label ?: return
            val leadingCharacter = activeLabel.substring(0, 1)

            activeGraphics.fill(' ')
            activeGraphics.putString(1, 0, activeLabel)
            if (activeMenuItem is Menu && activeMenuItem.parent !is MenuBar) {
                activeGraphics.putString(
                    (activeGraphics.size ?: TerminalSize.ZERO).columns - 2,
                    0,
                    Symbols.TRIANGLE_RIGHT_POINTING_BLACK.toString(),
                )
            }
            if (activeLabel.isNotEmpty()) {
                if (activeMenuItem.isFocused) {
                    activeGraphics.applyThemeStyle(themeDefinition.active)
                } else {
                    activeGraphics.applyThemeStyle(themeDefinition.preLight)
                }
                activeGraphics.putString(1, 0, leadingCharacter)
            }
        }
    }
}
