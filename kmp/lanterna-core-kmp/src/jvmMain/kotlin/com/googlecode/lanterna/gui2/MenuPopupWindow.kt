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
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.menu.MenuItem

/**
 * Popup window wrapper for menu items.
 */
class MenuPopupWindow(parent: Component?) : AbstractWindow() {
    private val menuItemPanel: Panel = Panel(LinearLayout(Direction.VERTICAL))

    init {
        setHints(listOf(Hint.MODAL, Hint.MENU_POPUP, Hint.FIXED_POSITION))
        if (parent != null) {
            val menuPositionGlobal = parent.toGlobal(TerminalPosition.TOP_LEFT_CORNER)
            position = menuPositionGlobal?.withRelative(0, 1)
        }
        component = menuItemPanel
    }

    fun addMenuItem(menuItem: MenuItem?) {
        requireNotNull(menuItem) { "menuItem cannot be null" }
        menuItemPanel.addComponent(menuItem)
        menuItem.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.FILL))
        if (menuItemPanel.childCount == 1) {
            focusedInteractable = menuItem
        }
        invalidate()
    }
}
