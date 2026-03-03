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
 * Copyright (C) 2017 Bruno Eberhard
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */
package com.googlecode.lanterna.gui2.menu

import com.googlecode.lanterna.gui2.MenuPopupWindow
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.WindowListenerAdapter
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

open class Menu(label: String) : MenuItem(label) {
    private val subItems: MutableList<MenuItem?> = ArrayList()

    open fun add(menuItem: MenuItem?): Menu {
        synchronized(subItems) {
            subItems.add(menuItem)
        }
        return this
    }

    open fun getSubMenus(): List<MenuItem?> {
        return ArrayList(subItems)
    }

    open fun setAccelerator(keyStroke: KeyStroke?): Menu {
        super.setAccelerator(keyStroke)
        return this
    }

    override fun onActivated(): Boolean {
        var result = true
        if (subItems.isEmpty()) {
            return result
        }
        val popupMenu = MenuPopupWindow(this)
        val popupCancelled = AtomicBoolean(false)
        for (menuItem in subItems) {
            popupMenu.addMenuItem(menuItem)
        }
        if (parent is MenuBar) {
            val menuBar = parent as MenuBar
            popupMenu.addWindowListener(object : WindowListenerAdapter() {
                override fun onUnhandledInput(
                    basePane: Window?,
                    keyStroke: KeyStroke,
                    hasBeenHandled: AtomicBoolean?
                ) {
                    if (keyStroke.keyType == KeyType.ARROW_LEFT) {
                        val thisMenuIndex = menuBar.childrenList.indexOf(this@Menu)
                        if (thisMenuIndex > 0) {
                            popupMenu.close()
                            val nextSelectedMenu = menuBar.getMenu(thisMenuIndex - 1)
                            nextSelectedMenu.takeFocus()
                            nextSelectedMenu.onActivated()
                        }
                    } else if (keyStroke.keyType == KeyType.ARROW_RIGHT) {
                        val thisMenuIndex = menuBar.childrenList.indexOf(this@Menu)
                        if (thisMenuIndex >= 0 && thisMenuIndex < menuBar.menuCount - 1) {
                            popupMenu.close()
                            val nextSelectedMenu = menuBar.getMenu(thisMenuIndex + 1)
                            nextSelectedMenu.takeFocus()
                            nextSelectedMenu.onActivated()
                        }
                    }

                    for (menuItem in subItems) {
                        val currentMenuItem = menuItem ?: throw NullPointerException()
                        if (currentMenuItem.isEnabled && currentMenuItem.isKeyboardAcceleratorStroke(keyStroke)) {
                            val result = currentMenuItem.handleKeyStroke(keyStroke)
                            if (result == Result.HANDLED) break
                        }
                    }
                }
            })
        }
        popupMenu.addWindowListener(object : WindowListenerAdapter() {
            override fun onUnhandledInput(
                basePane: Window?,
                keyStroke: KeyStroke,
                hasBeenHandled: AtomicBoolean?
            ) {
                if (keyStroke.keyType == KeyType.ESCAPE) {
                    popupCancelled.set(true)
                    popupMenu.close()
                }
            }
        })
        (textGUI as WindowBasedTextGUI).addWindowAndWait(popupMenu)
        result = !popupCancelled.get()

        return result
    }
}
